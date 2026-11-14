package pe.edu.upc.fiadito.platform.billing.interfaces.rest.resources;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** Pago registrado: tasa moratoria (solo si se pagó vencido), mora e imputación mora → interés → capital. */
public record PaymentResource(Long id, Long idNegocio, Long idVenta, Long idCuota, Long idSocio, String nombreSocio, String producto,
                              String concepto, LocalDate fechaPagoReal, Integer diasMora, String periodoTasaMora,
                              BigDecimal valorTasaMora, BigDecimal tedMora, BigDecimal interesMora, BigDecimal totalPagado,
                              BigDecimal imputacionMora, BigDecimal imputacionInteres, BigDecimal imputacionCapital, String medioPago,
                              LocalDateTime fechaRegistro) {
}
