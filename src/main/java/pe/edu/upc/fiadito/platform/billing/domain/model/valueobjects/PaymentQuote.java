package pe.edu.upc.fiadito.platform.billing.domain.model.valueobjects;

import pe.edu.upc.fiadito.platform.shared.domain.model.valueobjects.EffectiveRate;

import java.math.BigDecimal;

/**
 * Cotización de un pago: D (cuota = interés + capital), días de mora, tasa moratoria (solo si está vencida), TED moratoria,
 * interés moratorio, total e imputación mora → interés compensatorio → capital.
 *
 * @param lateRate tasa moratoria aplicada; null si se paga a tiempo
 */
public record PaymentQuote(BigDecimal amount, int lateDays, EffectiveRate lateRate, BigDecimal dailyRate, BigDecimal lateInterest,
                           BigDecimal total, BigDecimal lateImputation, BigDecimal interestImputation, BigDecimal capitalImputation) {
    public boolean isLate() {
        return lateDays > 0;
    }
}
