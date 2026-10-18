package pe.edu.upc.fiadito.platform.billing.domain.model.valueobjects;

import java.math.BigDecimal;

/**
 * Cotización de un pago: D, días de mora, TED moratoria, interés moratorio, total e imputación mora → capital.
 */
public record PaymentQuote(BigDecimal amount, int lateDays, BigDecimal dailyRate, BigDecimal lateInterest, BigDecimal total,
                           BigDecimal lateImputation, BigDecimal capitalImputation) {
}
