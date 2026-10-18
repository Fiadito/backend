package pe.edu.upc.fiadito.platform.credit.interfaces.rest.resources;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record SaleResource(Long id, Long idNegocio, Long idSocio, String nombreSocio, Long idProducto, String producto, Integer cantidad,
                           BigDecimal precioLista, BigDecimal monto,
                           @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime fechaHoraCompra,
                           String modalidad, Integer nCuotas, LocalDate fechaPago, BigDecimal cuota, BigDecimal totalAPagar, String estado,
                           List<InstallmentResource> cuotas) {
}
