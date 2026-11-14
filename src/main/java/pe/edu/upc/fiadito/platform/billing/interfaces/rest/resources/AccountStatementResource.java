package pe.edu.upc.fiadito.platform.billing.interfaces.rest.resources;

import pe.edu.upc.fiadito.platform.credit.interfaces.rest.resources.SaleResource;
import pe.edu.upc.fiadito.platform.membership.interfaces.rest.resources.MemberResource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Estado de cuenta (Figura 11): próximo pago, deuda capital, interés pendiente, mora estimada (solo lo vencido),
 * crédito disponible, planes de pago con interés y pagos.
 */
public record AccountStatementResource(MemberResource socio, LocalDate hoy, NextPaymentResource proximoPago, BigDecimal deudaCapital,
                                       BigDecimal interesPendiente, BigDecimal moraHoy, BigDecimal deudaActual,
                                       BigDecimal creditoDisponible, boolean conMora, List<SaleResource> ventas,
                                       List<ObligationResource> obligaciones, List<PendingObligationResource> pendientes,
                                       List<PaymentResource> pagos) {
}
