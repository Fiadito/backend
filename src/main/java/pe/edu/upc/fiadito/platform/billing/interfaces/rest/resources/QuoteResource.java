package pe.edu.upc.fiadito.platform.billing.interfaces.rest.resources;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Respuesta de POST /pagos/cotizacion: total exigible e imputación del pago.
 * vencida = false → no hay mora ni tasa moratoria (periodoTasaMora y valorTasaMora vienen nulos).
 */
public record QuoteResource(Long idVenta, Long idCuota, String nombreSocio, String concepto, LocalDate fechaVencimiento,
                            LocalDate fechaPagoReal, BigDecimal montoExigible, BigDecimal interesCompensatorio, BigDecimal capital,
                            boolean vencida, Integer diasMora, String periodoTasaMora, BigDecimal valorTasaMora, BigDecimal tedMora,
                            BigDecimal interesMora, BigDecimal totalExigible, BigDecimal imputacionMora, BigDecimal imputacionInteres,
                            BigDecimal imputacionCapital) {
}
