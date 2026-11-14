package pe.edu.upc.fiadito.platform.billing.interfaces.rest.resources;

import pe.edu.upc.fiadito.platform.shared.domain.model.valueobjects.RatePeriod;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Cuerpo de POST /pagos/cotizacion. periodoTasaMora y valorTasaMora solo se usan si la obligación está vencida (por defecto TEA 60 %). */
public record QuoteRequestResource(Long idVenta, Long idCuota, LocalDate fechaPagoReal, RatePeriod periodoTasaMora, BigDecimal valorTasaMora) {
}
