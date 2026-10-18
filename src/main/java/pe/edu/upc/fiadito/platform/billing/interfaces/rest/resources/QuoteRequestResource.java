package pe.edu.upc.fiadito.platform.billing.interfaces.rest.resources;

import java.time.LocalDate;

public record QuoteRequestResource(Long idVenta, Long idCuota, LocalDate fechaPagoReal) {
}
