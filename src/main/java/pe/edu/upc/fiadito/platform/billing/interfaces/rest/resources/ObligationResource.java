package pe.edu.upc.fiadito.platform.billing.interfaces.rest.resources;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Obligación (cuota o pago único) con los datos de su plan de pagos y su estado.
 */
public record ObligationResource(String clave, String codigo, Long idVenta, Long idCuota, Long idSocio, String nombreSocio,
                                 Long idProducto, String producto, Integer cantidad,
                                 @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime fechaHoraCompra,
                                 BigDecimal montoCompra, String concepto, Integer numero, Integer nCuotas, LocalDate fechaVencimiento,
                                 BigDecimal saldoInicial, BigDecimal amortizacion, BigDecimal monto, BigDecimal saldoFinal, String estado) {
}
