package pe.edu.upc.fiadito.platform;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Casos de prueba funcionales (CP01–CP12) y los cambios pedidos por el profesor, contra la API con la base H2 y los datos iniciales.
 * Fecha del sistema: 04/11/2026.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ApiFlowTests {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper json;

    private String login(String email, String password) throws Exception {
        var body = mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"" + email + "\",\"clave\":\"" + password + "\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return json.readTree(body).get("token").asText();
    }

    private JsonNode getJson(String token, String url) throws Exception {
        return json.readTree(mvc.perform(get(url).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
    }

    private JsonNode send(String method, String token, String url, String body, ResultMatcher expected) throws Exception {
        var request = switch (method) {
            case "POST" -> post(url);
            case "PUT" -> put(url);
            case "PATCH" -> patch(url);
            default -> throw new IllegalArgumentException(method);
        };
        var content = mvc.perform(request.header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(expected).andReturn().getResponse().getContentAsString();
        return content.isBlank() ? null : json.readTree(content);
    }

    private long memberId(String token, String dni) throws Exception {
        for (var m : getJson(token, "/api/v1/socios")) if (m.get("dni").asText().equals(dni)) return m.get("id").asLong();
        throw new AssertionError("Socio no encontrado: " + dni);
    }

    private long productId(String token, String descriptionStart) throws Exception {
        for (var p : getJson(token, "/api/v1/productos")) if (p.get("descripcion").asText().startsWith(descriptionStart)) return p.get("id").asLong();
        throw new AssertionError("Producto no encontrado: " + descriptionStart);
    }

    @Test
    @Order(1)
    void cp01_cp02_cp03_login() throws Exception {
        assertThat(login("josue.flores@fuerzanorte.pe", "Fiadito2026!")).isNotBlank();
        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"correo\":\"josue.flores@fuerzanorte.pe\",\"clave\":\"mala\"}")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/v1/socios")).andExpect(status().isUnauthorized());
    }

    @Test
    @Order(2)
    void cp04_socioNoPuedeRegistrarVentas() throws Exception {
        var token = login("carla.mendoza@correo.pe", "Socio2026!");
        send("POST", token, "/api/v1/ventas", "{}", status().isForbidden());
    }

    @Test
    @Order(3)
    void cp05_limiteFueraDeRango_yErroresNoCierranSesion() throws Exception {
        var token = login("josue.flores@fuerzanorte.pe", "Fiadito2026!");
        send("POST", token, "/api/v1/socios",
                "{\"dni\":\"71234567\",\"nombres\":\"Prueba Limite\",\"moneda\":\"PEN\",\"limiteCredito\":1200,\"maxCuotas\":8}",
                status().isBadRequest());
        // Un error de ruta o de método con un token válido ya no responde 401 (antes el frontend cerraba la sesión)
        mvc.perform(get("/api/v1/no-existe").header("Authorization", "Bearer " + token)).andExpect(status().isNotFound());
        mvc.perform(delete("/api/v1/socios/1").header("Authorization", "Bearer " + token)).andExpect(status().isMethodNotAllowed());
    }

    @Test
    @Order(4)
    void cp07_cp06_cp08_cp09_cp10_cp11_cp12() throws Exception {
        var token = login("josue.flores@fuerzanorte.pe", "Fiadito2026!");
        long carlaId = memberId(token, "72458813"), luisId = memberId(token, "70984512");
        long creatinaId = productId(token, "Creatina monohidratada micronizada 600 g"); // S/ 250

        // CP07: juego 1 con TEA 20 % → método francés: cuota S/ 64.66, interés total S/ 17.32
        var sale = "{\"idSocio\":" + carlaId + ",\"idProducto\":" + creatinaId + ",\"cantidad\":2,\"fechaHoraCompra\":\"2026-10-05T10:00\","
                + "\"modalidad\":\"CUOTAS\",\"nCuotas\":8,\"periodoTasa\":\"TEA\",\"valorTasa\":0.20}";
        send("POST", token, "/api/v1/ventas", sale.replace("0.20}", "0.60}"), status().isBadRequest()); // tasa fuera de 15–27 %
        var created = send("POST", token, "/api/v1/ventas", sale, status().isCreated());
        assertThat(created.get("cuota").decimalValue()).isEqualByComparingTo("64.66");
        assertThat(created.get("tasaQuincenal").decimalValue()).isEqualByComparingTo("0.007625660");
        assertThat(created.get("interesTotal").decimalValue()).isEqualByComparingTo("17.32");
        assertThat(created.get("totalAPagar").decimalValue()).isEqualByComparingTo("517.32");
        assertThat(created.get("cuotas").get(0).get("interes").decimalValue()).isEqualByComparingTo("3.81");
        assertThat(created.get("cuotas").get(0).get("fechaVencimiento").asText()).isEqualTo("2026-10-15");
        assertThat(created.get("cuotas").get(7).get("fechaVencimiento").asText()).isEqualTo("2027-01-30");
        long carlaSale = created.get("id").asLong();

        // CP06: crédito insuficiente (el límite se compara con el capital)
        send("POST", token, "/api/v1/ventas", sale.replace("\"cantidad\":2", "\"cantidad\":3"), status().isBadRequest());

        // CP08: reporte del corte 25/10/2026
        var report = getJson(token, "/api/v1/reportes/corte?fecha=2026-10-25");
        assertThat(report.get("fechaPago").asText()).isEqualTo("2026-10-30");
        assertThat(report.toString()).contains("Carla Mendoza").contains("Luis Ramírez").contains("Cuota 2 de 8");

        // CP09: cuota 2 de Luis pagada el 04/11/2026 → vencida 5 días; la tasa moratoria se coloca al pagar (TEA 60 % por defecto)
        long luisSale = 0, cuota2 = 0, cuota3 = 0;
        for (var v : getJson(token, "/api/v1/ventas?idSocio=" + luisId)) {
            luisSale = v.get("id").asLong();
            cuota2 = v.get("cuotas").get(1).get("id").asLong();
            cuota3 = v.get("cuotas").get(2).get("id").asLong();
        }
        var quoteBody = "{\"idVenta\":" + luisSale + ",\"idCuota\":" + cuota2 + ",\"fechaPagoReal\":\"2026-11-04\"";
        var quote = send("POST", token, "/api/v1/pagos/cotizacion", quoteBody + "}", status().isOk());
        assertThat(quote.get("vencida").asBoolean()).isTrue();
        assertThat(quote.get("periodoTasaMora").asText()).isEqualTo("TEA");
        assertThat(quote.get("diasMora").asInt()).isEqualTo(5);
        assertThat(quote.get("montoExigible").decimalValue()).isEqualByComparingTo("64.66");
        assertThat(quote.get("interesMora").decimalValue()).isEqualByComparingTo("0.42");
        assertThat(quote.get("totalExigible").decimalValue()).isEqualByComparingTo("65.08");
        send("POST", token, "/api/v1/pagos/cotizacion", quoteBody + ",\"periodoTasaMora\":\"TEA\",\"valorTasaMora\":0.30}", status().isBadRequest());

        // CP10: pago parcial rechazado; fecha futura rechazada; pago completo con imputación mora → interés → capital
        var payment = "{\"idVenta\":" + luisSale + ",\"idCuota\":" + cuota2 + ",\"medioPago\":\"EFECTIVO\",\"periodoTasaMora\":\"TEA\",\"valorTasaMora\":0.60,";
        send("POST", token, "/api/v1/pagos", payment + "\"fechaPagoReal\":\"2026-11-04\",\"montoPagado\":64.66}", status().isBadRequest());
        send("POST", token, "/api/v1/pagos", payment + "\"fechaPagoReal\":\"2026-11-20\",\"montoPagado\":70}", status().isBadRequest());
        var paid = send("POST", token, "/api/v1/pagos", payment + "\"fechaPagoReal\":\"2026-11-04\",\"montoPagado\":65.08}", status().isCreated());
        assertThat(paid.get("imputacionMora").decimalValue()).isEqualByComparingTo("0.42");
        assertThat(paid.get("imputacionInteres").decimalValue()).isEqualByComparingTo("3.35");
        assertThat(paid.get("imputacionCapital").decimalValue()).isEqualByComparingTo("61.31");
        send("POST", token, "/api/v1/pagos", payment + "\"fechaPagoReal\":\"2026-11-04\",\"montoPagado\":65.08}", status().isBadRequest());

        // Pago a tiempo: sin tasa ni interés moratorio
        var onTime = send("POST", token, "/api/v1/pagos/cotizacion",
                "{\"idVenta\":" + luisSale + ",\"idCuota\":" + cuota3 + ",\"fechaPagoReal\":\"2026-11-04\"}", status().isOk());
        assertThat(onTime.get("vencida").asBoolean()).isFalse();
        assertThat(onTime.get("periodoTasaMora").isNull()).isTrue();
        assertThat(onTime.get("interesMora").decimalValue()).isEqualByComparingTo("0.00");
        assertThat(onTime.get("totalExigible").decimalValue()).isEqualByComparingTo("64.66");

        // CP11: el socio ve solo su estado de cuenta (capital e interés pendiente)
        var carlaToken = login("carla.mendoza@correo.pe", "Socio2026!");
        var statement = getJson(carlaToken, "/api/v1/socios/me/estado-cuenta");
        assertThat(statement.get("socio").get("dni").asText()).isEqualTo("72458813");
        assertThat(statement.get("ventas")).hasSize(1);
        assertThat(statement.get("deudaCapital").decimalValue()).isEqualByComparingTo("500.00");
        assertThat(statement.get("interesPendiente").decimalValue()).isEqualByComparingTo("17.32");

        // Cambio de tasa después de registrar la venta: se recalculan las cuotas pendientes (todas, porque Carla no pagó)
        send("PATCH", token, "/api/v1/ventas/" + carlaSale + "/tasa", "{\"periodoTasa\":\"TEA\",\"valorTasa\":0.30}", status().isBadRequest());
        var changed = send("PATCH", token, "/api/v1/ventas/" + carlaSale + "/tasa", "{\"periodoTasa\":\"TEA\",\"valorTasa\":0.27}", status().isOk());
        assertThat(changed.get("cuota").decimalValue()).isEqualByComparingTo("65.35");
        assertThat(changed.get("interesTotal").decimalValue()).isEqualByComparingTo("22.77");
        assertThat(changed.get("totalAPagar").decimalValue()).isEqualByComparingTo("522.77");
        assertThat(changed.get("cuotas").get(7).get("montoCuota").decimalValue()).isEqualByComparingTo("65.32");

        // Cambio de tasa con cuotas pagadas: Luis (cuotas 1 y 2 pagadas) pasa a TEA 27 % → 6 cuotas recalculadas desde S/ 377.84
        var luisChanged = send("PATCH", token, "/api/v1/ventas/" + luisSale + "/tasa", "{\"periodoTasa\":\"TEA\",\"valorTasa\":0.27}", status().isOk());
        assertThat(luisChanged.get("cuotas").get(0).get("montoCuota").decimalValue()).isEqualByComparingTo("64.66");
        assertThat(luisChanged.get("cuotas").get(2).get("saldoInicial").decimalValue()).isEqualByComparingTo("377.84");
        assertThat(luisChanged.get("cuotas").get(7).get("saldoFinal").decimalValue()).isEqualByComparingTo("0.00");

        // El límite no puede quedar por debajo de la deuda (Carla debe S/ 500)
        send("PUT", token, "/api/v1/socios/" + carlaId,
                "{\"dni\":\"72458813\",\"nombres\":\"Carla Mendoza Ríos\",\"moneda\":\"PEN\",\"limiteCredito\":400,\"maxCuotas\":8}",
                status().isBadRequest());

        // CP12: bitácora
        var log = getJson(token, "/api/v1/bitacora").toString();
        assertThat(log).contains("LOGIN").contains("LOGIN_FALLIDO").contains("VENDER").contains("PAGAR").contains("Cambio de tasa");

        // Panel
        var panel = getJson(token, "/api/v1/panel");
        assertThat(panel.get("plan").size()).isGreaterThan(0);
    }

    @Test
    @Order(5)
    void combosDe100_200y400LleganAlMinimoDeCompra() throws Exception {
        var token = login("josue.flores@fuerzanorte.pe", "Fiadito2026!");
        var combos = new java.util.ArrayList<JsonNode>();
        getJson(token, "/api/v1/productos").forEach(p -> { if (p.get("tipo").asText().equals("COMBO")) combos.add(p); });
        assertThat(combos).extracting(c -> c.get("precioLista").decimalValue().intValue()).containsOnly(100, 200, 400);
        assertThat(combos).filteredOn(c -> c.get("precioLista").decimalValue().intValue() > 300).hasSizeGreaterThanOrEqualTo(3);
        assertThat(combos).allSatisfy(c -> assertThat(c.get("items").size()).isGreaterThan(0));

        long valeria = memberId(token, "30567891");
        long combo100 = productId(token, "Combo Hidratación");
        var base = "{\"idSocio\":" + valeria + ",\"idProducto\":" + combo100 + ",\"fechaHoraCompra\":\"2026-11-03T09:00\","
                + "\"modalidad\":\"CUOTAS\",\"nCuotas\":4,\"periodoTasa\":\"TEQ\",\"valorTasa\":0.01,";
        send("POST", token, "/api/v1/ventas", base + "\"cantidad\":3}", status().isBadRequest()); // S/ 300 < S/ 400
        var sale = send("POST", token, "/api/v1/ventas", base + "\"cantidad\":4}", status().isCreated());
        assertThat(sale.get("monto").decimalValue()).isEqualByComparingTo("400.00");

        // Un combo debe costar 100, 200 o 400
        send("POST", token, "/api/v1/productos", "{\"tipo\":\"COMBO\",\"descripcion\":\"Combo raro\",\"marca\":\"Fuerza Norte\","
                + "\"precioLista\":150,\"items\":[{\"idProducto\":1,\"cantidad\":10}]}", status().isBadRequest());
    }
}
