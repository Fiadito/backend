package pe.edu.upc.fiadito.platform.billing.interfaces.rest.resources;

import java.math.BigDecimal;
import java.util.List;

/** Panel del negocio: resumen y plan de pagos de los créditos vigentes. */
public record PanelResource(BigDecimal totalPorCobrar, Integer creditosVigentes, BigDecimal montoVencido, Integer obligacionesVencidas,
                            NextPaymentResource proximoPago, Integer sociosEnMora, Integer sociosConCredito, List<ObligationResource> plan) {
}
