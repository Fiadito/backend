package pe.edu.upc.fiadito.platform.shared.infrastructure.persistence.jpa.configuration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Agrega en MySQL las claves foráneas entre contextos y las restricciones CHECK del informe (sección 9 b).
 * Los agregados se referencian por id (DDD), así que Hibernate no crea esas FK; aquí se crean una sola vez.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseConstraintsInitializer {

    private final JdbcTemplate jdbc;

    private static final Map<String, String> CONSTRAINTS = new LinkedHashMap<>();

    static {
        CONSTRAINTS.put("fk_usuario_negocio", "ALTER TABLE usuario ADD CONSTRAINT fk_usuario_negocio FOREIGN KEY (id_negocio) REFERENCES negocio (id_negocio)");
        CONSTRAINTS.put("fk_usuario_socio", "ALTER TABLE usuario ADD CONSTRAINT fk_usuario_socio FOREIGN KEY (id_socio) REFERENCES socio (id_socio)");
        CONSTRAINTS.put("fk_socio_negocio", "ALTER TABLE socio ADD CONSTRAINT fk_socio_negocio FOREIGN KEY (id_negocio) REFERENCES negocio (id_negocio)");
        CONSTRAINTS.put("fk_producto_negocio", "ALTER TABLE producto ADD CONSTRAINT fk_producto_negocio FOREIGN KEY (id_negocio) REFERENCES negocio (id_negocio)");
        CONSTRAINTS.put("fk_venta_negocio", "ALTER TABLE venta ADD CONSTRAINT fk_venta_negocio FOREIGN KEY (id_negocio) REFERENCES negocio (id_negocio)");
        CONSTRAINTS.put("fk_venta_socio", "ALTER TABLE venta ADD CONSTRAINT fk_venta_socio FOREIGN KEY (id_socio) REFERENCES socio (id_socio)");
        CONSTRAINTS.put("fk_venta_producto", "ALTER TABLE venta ADD CONSTRAINT fk_venta_producto FOREIGN KEY (id_producto) REFERENCES producto (id_producto)");
        CONSTRAINTS.put("fk_pago_negocio", "ALTER TABLE pago ADD CONSTRAINT fk_pago_negocio FOREIGN KEY (id_negocio) REFERENCES negocio (id_negocio)");
        CONSTRAINTS.put("fk_pago_venta", "ALTER TABLE pago ADD CONSTRAINT fk_pago_venta FOREIGN KEY (id_venta) REFERENCES venta (id_venta)");
        CONSTRAINTS.put("fk_pago_cuota", "ALTER TABLE pago ADD CONSTRAINT fk_pago_cuota FOREIGN KEY (id_cuota) REFERENCES cuota (id_cuota)");
        CONSTRAINTS.put("fk_pago_socio", "ALTER TABLE pago ADD CONSTRAINT fk_pago_socio FOREIGN KEY (id_socio) REFERENCES socio (id_socio)");
        CONSTRAINTS.put("fk_bitacora_negocio", "ALTER TABLE bitacora ADD CONSTRAINT fk_bitacora_negocio FOREIGN KEY (id_negocio) REFERENCES negocio (id_negocio)");
        CONSTRAINTS.put("fk_bitacora_usuario", "ALTER TABLE bitacora ADD CONSTRAINT fk_bitacora_usuario FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario)");
        CONSTRAINTS.put("ck_socio_tasa_mora", "ALTER TABLE socio ADD CONSTRAINT ck_socio_tasa_mora CHECK (valor_tasa_mora >= 0.40)");
        CONSTRAINTS.put("ck_socio_periodo", "ALTER TABLE socio ADD CONSTRAINT ck_socio_periodo CHECK (periodo_tasa_mora IN ('TEA','TES','TET','TEM','TEQ'))");
        CONSTRAINTS.put("ck_socio_limite", "ALTER TABLE socio ADD CONSTRAINT ck_socio_limite CHECK (limite_credito BETWEEN 400 AND 1000)");
        CONSTRAINTS.put("ck_socio_cuotas", "ALTER TABLE socio ADD CONSTRAINT ck_socio_cuotas CHECK (max_cuotas BETWEEN 1 AND 8)");
        CONSTRAINTS.put("ck_producto_precios", "ALTER TABLE producto ADD CONSTRAINT ck_producto_precios CHECK (precio_contado > 0 AND precio_lista >= precio_contado)");
        CONSTRAINTS.put("ck_venta_monto", "ALTER TABLE venta ADD CONSTRAINT ck_venta_monto CHECK (monto BETWEEN 400 AND 1000)");
        CONSTRAINTS.put("ck_venta_cuotas", "ALTER TABLE venta ADD CONSTRAINT ck_venta_cuotas CHECK (n_cuotas BETWEEN 1 AND 8)");
        CONSTRAINTS.put("ck_pago_medio", "ALTER TABLE pago ADD CONSTRAINT ck_pago_medio CHECK (medio_pago IN ('EFECTIVO','YAPE','PLIN','TRANSFERENCIA'))");
    }

    @Order(1)
    @EventListener(ApplicationReadyEvent.class)
    public void addConstraints() {
        String product = jdbc.execute((java.sql.Connection c) -> c.getMetaData().getDatabaseProductName());
        if (product == null || !(product.contains("MySQL") || product.contains("MariaDB"))) return;
        CONSTRAINTS.forEach((name, sql) -> {
            Integer exists = jdbc.queryForObject("SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS "
                    + "WHERE CONSTRAINT_SCHEMA = DATABASE() AND CONSTRAINT_NAME = ?", Integer.class, name);
            if (exists != null && exists > 0) return;
            try {
                jdbc.execute(sql);
                log.info("Restricción creada: {}", name);
            } catch (Exception e) {
                log.warn("No se pudo crear la restricción {}: {}", name, e.getMessage());
            }
        });
    }
}
