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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Casos de prueba funcionales de la Tabla 12 (CP01–CP12) contra la API con la base H2 y los datos iniciales.
 * Fecha del sistema: 17/10/2026.
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
        mvc.perform(post("/api/v1/ventas").header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(3)
    void cp05_limiteFueraDeRango() throws Exception {
        var token = login("josue.flores@fuerzanorte.pe", "Fiadito2026!");
        mvc.perform(post("/api/v1/socios").header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"dni\":\"71234567\",\"nombres\":\"Prueba Limite\",\"moneda\":\"PEN\",\"periodoTasaMora\":\"TEA\","
                                + "\"valorTasaMora\":0.6,\"limiteCredito\":1200,\"maxCuotas\":8}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(4)
    void cp07_cp06_cp08_cp09_cp10_cp11_cp12() throws Exception {
        var token = login("josue.flores@fuerzanorte.pe", "Fiadito2026!");
        var carla = getJson(token, "/api/v1/socios").findValues("nombres").stream().toList();
        assertThat(carla).isNotEmpty();
        long carlaId = 0, luisId = 0, xbId = 0;
        for (var m : getJson(token, "/api/v1/socios")) {
            if (m.get("dni").asText().equals("72458813")) carlaId = m.get("id").asLong();
            if (m.get("dni").asText().equals("70984512")) luisId = m.get("id").asLong();
        }
        for (var p : getJson(token, "/api/v1/productos")) if (p.get("descripcion").asText().equals("Bebida energizante sabor Piña")) xbId = p.get("id").asLong();

        // CP07: juego 1
        var sale = "{\"idSocio\":" + carlaId + ",\"idProducto\":" + xbId + ",\"cantidad\":100,\"fechaHoraCompra\":\"2026-10-05T10:00\",\"modalidad\":\"CUOTAS\",\"nCuotas\":8}";
        var created = json.readTree(mvc.perform(post("/api/v1/ventas").header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content(sale)).andExpect(status().isCreated()).andReturn().getResponse().getContentAsString());
        assertThat(created.get("cuota").decimalValue()).isEqualByComparingTo("62.50");
        assertThat(created.get("cuotas").get(0).get("fechaVencimiento").asText()).isEqualTo("2026-10-15");
        assertThat(created.get("cuotas").get(7).get("fechaVencimiento").asText()).isEqualTo("2027-01-30");

        // CP06: crédito insuficiente
        var tooMuch = sale.replace("\"cantidad\":100", "\"cantidad\":120");
        mvc.perform(post("/api/v1/ventas").header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON).content(tooMuch))
                .andExpect(status().isBadRequest());

        // CP08: reporte del corte 25/10/2026
        var report = getJson(token, "/api/v1/reportes/corte?fecha=2026-10-25");
        assertThat(report.get("fechaPago").asText()).isEqualTo("2026-10-30");
        assertThat(report.toString()).contains("Carla Mendoza").contains("Cuota 2 de 8");

        // CP09: cuota 2 de Luis pagada el 04/11/2026
        long luisSale = 0, cuota2 = 0;
        for (var v : getJson(token, "/api/v1/ventas?idSocio=" + luisId)) {
            luisSale = v.get("id").asLong();
            cuota2 = v.get("cuotas").get(1).get("id").asLong();
        }
        var quote = json.readTree(mvc.perform(post("/api/v1/pagos/cotizacion").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idVenta\":" + luisSale + ",\"idCuota\":" + cuota2 + ",\"fechaPagoReal\":\"2026-11-04\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString());
        assertThat(quote.get("diasMora").asInt()).isEqualTo(5);
        assertThat(quote.get("interesMora").decimalValue()).isEqualByComparingTo("0.41");
        assertThat(quote.get("totalExigible").decimalValue()).isEqualByComparingTo("62.91");

        // CP10: pago parcial rechazado
        var payment = "{\"idVenta\":" + luisSale + ",\"idCuota\":" + cuota2 + ",\"fechaPagoReal\":\"2026-11-04\",\"medioPago\":\"EFECTIVO\",\"montoPagado\":";
        mvc.perform(post("/api/v1/pagos").header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON).content(payment + "60}"))
                .andExpect(status().isBadRequest());
        var paid = json.readTree(mvc.perform(post("/api/v1/pagos").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(payment + "62.91}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString());
        assertThat(paid.get("imputacionMora").decimalValue()).isEqualByComparingTo("0.41");
        assertThat(paid.get("imputacionCapital").decimalValue()).isEqualByComparingTo("62.50");

        // CP11: el socio ve solo su estado de cuenta
        var carlaToken = login("carla.mendoza@correo.pe", "Socio2026!");
        var statement = getJson(carlaToken, "/api/v1/socios/me/estado-cuenta");
        assertThat(statement.get("socio").get("dni").asText()).isEqualTo("72458813");
        assertThat(statement.get("ventas")).hasSize(1);
        assertThat(statement.get("deudaCapital").decimalValue()).isEqualByComparingTo("500.00");

        // CP12: bitácora
        var log = getJson(token, "/api/v1/bitacora").toString();
        assertThat(log).contains("LOGIN").contains("LOGIN_FALLIDO").contains("VENDER").contains("PAGAR");

        // Panel
        var panel = getJson(token, "/api/v1/panel");
        assertThat(panel.get("plan").size()).isGreaterThan(0);
    }
}
