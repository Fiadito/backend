package pe.edu.upc.fiadito.platform.billing.interfaces.rest.resources;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Respuesta de POST /pagos/cotizacion: total exigible e imputación del pago. */
public record QuoteResource(Long idVenta, Long idCuota, String nombreSocio, String concepto, LocalDate fechaVencimiento,
                            LocalDate fechaPagoReal, BigDecimal montoExigible, Integer diasMora, String periodoTasaMora,
                            BigDecimal valorTasaMora, BigDecimal tedMora, BigDecimal interesMora, BigDecimal totalExigible,
                            BigDecimal imputacionMora, BigDecimal imputacionCapital) {
}
