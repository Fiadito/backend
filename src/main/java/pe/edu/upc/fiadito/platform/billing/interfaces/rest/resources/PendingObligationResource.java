package pe.edu.upc.fiadito.platform.billing.interfaces.rest.resources;

import java.math.BigDecimal;

/** Obligación pendiente con los días de mora, la mora y el total a hoy. */
public record PendingObligationResource(ObligationResource obligacion, Integer diasMora, BigDecimal moraHoy, BigDecimal totalHoy) {
}
