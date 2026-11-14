package pe.edu.upc.fiadito.platform.bootstrap;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import pe.edu.upc.fiadito.platform.billing.domain.model.commands.QuotePaymentCommand;
import pe.edu.upc.fiadito.platform.billing.domain.model.commands.RegisterPaymentCommand;
import pe.edu.upc.fiadito.platform.billing.domain.model.valueobjects.PaymentMethod;
import pe.edu.upc.fiadito.platform.billing.domain.services.PaymentCommandService;
import pe.edu.upc.fiadito.platform.catalog.domain.model.aggregates.Product;
import pe.edu.upc.fiadito.platform.catalog.domain.model.commands.SaveProductCommand;
import pe.edu.upc.fiadito.platform.catalog.domain.model.valueobjects.ComboItem;
import pe.edu.upc.fiadito.platform.catalog.domain.model.valueobjects.ProductType;
import pe.edu.upc.fiadito.platform.catalog.domain.model.valueobjects.SaleMode;
import pe.edu.upc.fiadito.platform.catalog.domain.services.ProductCommandService;
import pe.edu.upc.fiadito.platform.credit.domain.model.aggregates.CreditSale;
import pe.edu.upc.fiadito.platform.credit.domain.model.commands.RegisterSaleCommand;
import pe.edu.upc.fiadito.platform.credit.domain.model.valueobjects.PaymentMode;
import pe.edu.upc.fiadito.platform.credit.domain.model.valueobjects.SaleRequest;
import pe.edu.upc.fiadito.platform.credit.domain.services.CreditSaleCommandService;
import pe.edu.upc.fiadito.platform.iam.domain.model.commands.RegisterBusinessCommand;
import pe.edu.upc.fiadito.platform.iam.domain.services.IamCommandService;
import pe.edu.upc.fiadito.platform.iam.infrastructure.persistence.jpa.repositories.BusinessRepository;
import pe.edu.upc.fiadito.platform.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import pe.edu.upc.fiadito.platform.membership.domain.model.aggregates.Member;
import pe.edu.upc.fiadito.platform.membership.domain.model.commands.ChangeMemberStatusCommand;
import pe.edu.upc.fiadito.platform.membership.domain.model.commands.CreateMemberCommand;
import pe.edu.upc.fiadito.platform.membership.domain.model.valueobjects.Currency;
import pe.edu.upc.fiadito.platform.membership.domain.model.valueobjects.RecordStatus;
import pe.edu.upc.fiadito.platform.membership.domain.services.MemberCommandService;
import pe.edu.upc.fiadito.platform.shared.domain.model.valueobjects.RatePeriod;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Datos iniciales cuando la base está vacía: Fuerza Norte Gym, los productos de la Tabla 4 del informe,
 * siete socios y los juegos de prueba (sección 6 d). Usa los mismos servicios de la API, así los
 * cálculos y la bitácora son reales. Desactívalo con SEED_ENABLED=false.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FiaditoDataSeeder {

    public static final String BUSINESS_EMAIL = "josue.flores@fuerzanorte.pe";
    public static final String BUSINESS_PASSWORD = "Fiadito2026!";
    public static final String MEMBER_PASSWORD = "Socio2026!";

    private final BusinessRepository businessRepository;
    private final UserRepository userRepository;
    private final IamCommandService iam;
    private final MemberCommandService members;
    private final ProductCommandService products;
    private final CreditSaleCommandService sales;
    private final PaymentCommandService payments;

    @Value("${app.seed.enabled:true}")
    private boolean enabled;

    @Order(2)
    @EventListener(ApplicationReadyEvent.class)
    public void seed() {
        if (!enabled || businessRepository.count() > 0) return;
        try {
            run();
            log.info("Datos iniciales cargados. Negocio: {} / {} · Socios: <correo> / {}", BUSINESS_EMAIL, BUSINESS_PASSWORD, MEMBER_PASSWORD);
        } catch (Exception e) {
            log.warn("No se pudieron cargar todos los datos iniciales: {}", e.getMessage());
        }
    }

    private void run() {
        var business = iam.handle(new RegisterBusinessCommand("20605478123", "Fuerza Norte Gym", "Gimnasio",
                "Av. Universitaria 4520, Los Olivos", "Josué Flores", BUSINESS_EMAIL, BUSINESS_PASSWORD, BUSINESS_PASSWORD));
        Long bId = business.getId();
        Long actor = userRepository.findByEmail(BUSINESS_EMAIL).orElseThrow().getId();
        String actorName = "Josué Flores";

        // Productos de la Tabla 4
        Object[][] catalog = {
                {"Cielo", "Botella", "Agua mineral", "2.00", "agua"},
                {"Cielo", "Botella", "Agua mineral 1 litro", "2.50", "agua"},
                {"Hey Fit", "Botella", "Bebida funcional sabor Piña - Coco", "3.00", "bebida"},
                {"Hey Fit", "Botella", "Bebida funcional sabor Toronja - Camu Camu", "3.00", "bebida"},
                {"Hey Fit", "Botella", "Bebida funcional sabor Golden Fizz", "3.00", "bebida"},
                {"Hey Fit", "Botella", "Bebida funcional sabor Black Fizz", "3.00", "bebida"},
                {"Hey Fit", "Lata", "Bebida funcional sabor Piña - Coco", "3.50", "lata"},
                {"Hey Fit", "Lata", "Bebida funcional sabor Toronja - Camu Camu", "3.50", "lata"},
                {"Hey Fit", "Lata", "Bebida funcional sabor Golden Fizz", "3.50", "lata"},
                {"Hey Fit", "Lata", "Bebida funcional sabor Black Fizz", "3.50", "lata"},
                {"Gatorade", "Botella", "Bebida rehidratante sabor Tropical", "2.50", "bebida"},
                {"Gatorade", "Botella", "Bebida rehidratante sabor Ponche de Frutas", "2.50", "bebida"},
                {"Volt", "Botella", "Bebida energizante", "3.00", "energizante"},
                {"Volt", "Lata", "Bebida energizante", "3.50", "lata"},
                {"X-B", "Botella", "Bebida energizante sabor Piña", "5.00", "energizante"},
                {"X-B", "Botella", "Bebida energizante sabor Guaraná", "5.00", "energizante"},
                {"X-B", "Botella", "Bebida energizante sabor Blueberry", "5.00", "energizante"},
                {"Optimum Nutrition", "Frasco", "Proteína whey 1 kg sabor Chocolate", "185.00", "proteina"},
                {"Optimum Nutrition", "Frasco", "Proteína whey 1 kg sabor Vainilla", "185.00", "proteina"},
                {"Optimum Nutrition", "Bolsa", "Proteína whey 2.5 kg sabor Chocolate", "395.00", "proteina"},
                {"Dymatize ISO100", "Bolsa", "Proteína aislada de suero 2.5 kg sabor Vainilla", "420.00", "proteina"},
                {"MuscleTech Nitro-Tech", "Frasco", "Proteína whey con creatina 2 kg sabor Chocolate", "320.00", "proteina"},
                {"Optimum Nutrition", "Barra", "Barra de proteína sabor Chocolate", "11.00", "barra"},
                {"Quest", "Barra", "Barra de proteína sabor Cookies & Cream", "12.00", "barra"},
                {"Optimum Nutrition", "Pote", "Creatina monohidratada 300 g", "110.00", "creatina"},
                {"Fuerza Norte", "Unidad", "Shaker mezclador de 600 ml", "25.00", "shaker"}
        };
        Map<String, Product> productByName = new HashMap<>();
        for (Object[] p : catalog) {
            var price = new BigDecimal((String) p[3]);
            var supplier = "Fuerza Norte".equals(p[0]) ? "Fuerza Norte Gym" : "Distribuidora Deportiva SAC";
            var saved = products.handle(new SaveProductCommand(bId, null, ProductType.PRODUCTO, (String) p[2], (String) p[0], supplier,
                    (String) p[1], "/images/products/" + p[4] + ".svg", price, price, SaleMode.AMBAS, List.of()), actor, actorName);
            productByName.put(p[0] + "|" + p[1] + "|" + p[2], saved);
        }

        // Combos de S/ 100, 200 y 400: así las bebidas de S/ 2–5 llegan al mínimo de S/ 400 sin vender 100+ unidades sueltas
        var hydration = combo(bId, "Combo Hidratación (20 Cielo + 24 Gatorade)", "100.00", List.of(
                item(productByName, "Cielo|Botella|Agua mineral", 20), item(productByName, "Gatorade|Botella|Bebida rehidratante sabor Tropical", 24)),
                actor, actorName);
        combo(bId, "Combo Energía (28 X-B + 20 Volt)", "200.00", List.of(
                item(productByName, "X-B|Botella|Bebida energizante sabor Piña", 20), item(productByName, "X-B|Botella|Bebida energizante sabor Guaraná", 8),
                item(productByName, "Volt|Botella|Bebida energizante", 20)), actor, actorName);
        combo(bId, "Combo Fit (90 Hey Fit + 10 barras)", "400.00", List.of(
                item(productByName, "Hey Fit|Botella|Bebida funcional sabor Piña - Coco", 40),
                item(productByName, "Hey Fit|Botella|Bebida funcional sabor Golden Fizz", 10),
                item(productByName, "Hey Fit|Lata|Bebida funcional sabor Black Fizz", 40),
                item(productByName, "Optimum Nutrition|Barra|Barra de proteína sabor Chocolate", 10)), actor, actorName);

        // Socios (sin tasas: la tasa efectiva va en cada venta y la moratoria se coloca al pagar algo vencido)
        var carla = member(bId, "72458813", "Carla Mendoza Ríos", "987451220", "Calle Los Pinos 118, Los Olivos", "1000", "carla.mendoza@correo.pe", actor, actorName);
        var luis = member(bId, "70984512", "Luis Ramírez Paredes", "956321478", "Jr. Las Gardenias 230, Comas", "1000", "luis.ramirez@correo.pe", actor, actorName);
        var ana = member(bId, "43125678", "Ana Torres Vega", "945112233", "Av. Naranjal 845, Independencia", "800", "ana.torres@correo.pe", actor, actorName);
        var diego = member(bId, "87654321", "Diego Salazar Cruz", "912345678", "Calle Las Magnolias 77, Los Olivos", "600", "diego.salazar@correo.pe", actor, actorName);
        var jorge = member(bId, "98765432", "Jorge Castillo Rojas", "934567812", "Av. Carlos Izaguirre 1500, Los Olivos", "800", "jorge.castillo@correo.pe", actor, actorName);
        var maria = member(bId, "23456789", "María López Gómez", "967812345", "Jr. Huánuco 410, San Martín de Porres", "700", null, actor, actorName);
        member(bId, "30567891", "Valeria Núñez Soto", "978123456", "Calle Los Cedros 59, Los Olivos", "1000", null, actor, actorName);
        members.handle(new ChangeMemberStatusCommand(bId, maria.getId(), RecordStatus.INACTIVO), actor, actorName);

        // Jorge: 2 × MuscleTech (S/ 640) en 4 cuotas a TEA 24 %; pagó la cuota 1 con 2 días de atraso (mora TEA 60 %)
        var jorgeSale = sale(bId, jorge, productByName.get("MuscleTech Nitro-Tech|Frasco|Proteína whey con creatina 2 kg sabor Chocolate"), 2,
                LocalDateTime.of(2026, 9, 20, 19, 0), PaymentMode.CUOTAS, 4, "0.24", actor, actorName);
        pay(bId, jorgeSale, 1, LocalDate.of(2026, 10, 2), PaymentMethod.YAPE, actor, actorName);
        // Juego 2 (CP08–CP09): Luis, 100 × X-B Piña (S/ 500) en 8 cuotas a TEA 20 %; pagó la cuota 1 a tiempo
        var luisSale = sale(bId, luis, productByName.get("X-B|Botella|Bebida energizante sabor Piña"), 100,
                LocalDateTime.of(2026, 10, 8, 10, 30), PaymentMode.CUOTAS, 8, "0.20", actor, actorName);
        pay(bId, luisSale, 1, LocalDate.of(2026, 10, 15), PaymentMethod.EFECTIVO, actor, actorName);
        // Diego: Dymatize ISO100 (S/ 420) a pago único a TEA 18 % → vence el 30/10
        sale(bId, diego, productByName.get("Dymatize ISO100|Bolsa|Proteína aislada de suero 2.5 kg sabor Vainilla"), 1,
                LocalDateTime.of(2026, 10, 12, 17, 15), PaymentMode.PAGO_UNICO, 1, "0.18", actor, actorName);
        // Ana: 4 × Combo Hidratación (S/ 400) en 4 cuotas a TEA 22 %
        sale(bId, ana, hydration, 4, LocalDateTime.of(2026, 10, 20, 18, 30), PaymentMode.CUOTAS, 4, "0.22", actor, actorName);
        // Carla queda sin compras para ejecutar CP07 (juego 1) desde la web
        log.debug("Socio para CP07: {}", carla.getFullName());
    }

    private Member member(Long bId, String dni, String name, String phone, String address, String limit, String email,
                          Long actor, String actorName) {
        return members.handle(new CreateMemberCommand(bId, dni, name, phone, address, Currency.PEN, new BigDecimal(limit), 8, email,
                email == null ? null : MEMBER_PASSWORD), actor, actorName);
    }

    private Product combo(Long bId, String name, String price, List<ComboItem> items, Long actor, String actorName) {
        var value = new BigDecimal(price);
        return products.handle(new SaveProductCommand(bId, null, ProductType.COMBO, name, "Fuerza Norte", "Fuerza Norte Gym", "Combo",
                "/images/products/combo.svg", value, value, SaleMode.AMBAS, items), actor, actorName);
    }

    /** @param key "marca|unidad|descripción" (hay bebidas con el mismo nombre en botella y en lata) */
    private static ComboItem item(Map<String, Product> productByName, String key, int quantity) {
        var product = productByName.get(key);
        if (product == null) throw new IllegalStateException("Producto del combo no encontrado: " + key);
        return new ComboItem(product.getId(), quantity);
    }

    private CreditSale sale(Long bId, Member member, Product product, int quantity, LocalDateTime when, PaymentMode mode, int n,
                            String tea, Long actor, String actorName) {
        return sales.handle(new RegisterSaleCommand(bId, new SaleRequest(member.getId(), product.getId(), quantity, when, mode, n,
                RatePeriod.TEA, new BigDecimal(tea))), actor, actorName);
    }

    /** Paga una cuota con su total; si está vencida usa la tasa moratoria por defecto (TEA 60 %). */
    private void pay(Long bId, CreditSale sale, int number, LocalDate date, PaymentMethod method, Long actor, String actorName) {
        var installmentId = sale.getInstallments().stream().filter(i -> i.getNumber() == number).findFirst().orElseThrow().getId();
        var quote = payments.handle(new QuotePaymentCommand(bId, sale.getId(), installmentId, date, null, null)).quote();
        payments.handle(new RegisterPaymentCommand(bId, sale.getId(), installmentId, date, method, quote.total(), null, null), actor, actorName);
    }
}
