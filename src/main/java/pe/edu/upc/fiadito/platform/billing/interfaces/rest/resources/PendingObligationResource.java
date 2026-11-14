package pe.edu.upc.fiadito.platform.billing.interfaces.rest.resources;

import java.math.BigDecimal;

/**
 * Obligación pendiente. Si está vencida trae los días de atraso y la mora estimada a hoy con TEA 60 %
 * (la tasa real se coloca al registrar el pago); si no, diasMora = 0 y moraHoy = 0.
 */
public record PendingObligationResource(ObligationResource obligacion, Integer diasMora, BigDecimal moraHoy, BigDecimal totalHoy) {
}
