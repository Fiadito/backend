package pe.edu.upc.fiadito.platform.credit.interfaces.rest.resources;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Venta con su tasa efectiva (periodo, valor, TEQ de las cuotas y TEA equivalente), interés total y plan de pagos.
 */
public record SaleResource(Long id, Long idNegocio, Long idSocio, String nombreSocio, Long idProducto, String producto, Integer cantidad,
                           BigDecimal precioLista, BigDecimal monto,
                           @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime fechaHoraCompra,
                           String modalidad, Integer nCuotas, String periodoTasa, BigDecimal valorTasa, BigDecimal tasaQuincenal,
                           BigDecimal teaEquivalente, LocalDate fechaPago, BigDecimal cuota, BigDecimal interesTotal,
                           BigDecimal totalAPagar, String estado, List<InstallmentResource> cuotas) {
}
