package pe.edu.upc.fiadito.platform.billing.domain.model.valueobjects;

import pe.edu.upc.fiadito.platform.shared.domain.model.valueobjects.EffectiveRate;
import pe.edu.upc.fiadito.platform.shared.domain.model.valueobjects.Rounding;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Servicio de dominio: interés moratorio e imputación del pago (módulo 7.9).
 * Solo hay mora si el pago es posterior al vencimiento: dm = Freal − Fvencimiento;
 * TED = (1 + TEP_mor)^(1/N) − 1; Imor = D × ((1 + TED)^dm − 1); Total = D + Imor.
 * Imputación: mora → interés compensatorio de la cuota → capital (amortización).
 */
public final class LateInterest {

    private LateInterest() {
    }

    public static int lateDays(LocalDate dueDate, LocalDate paymentDate) {
        return (int) Math.max(0, ChronoUnit.DAYS.between(dueDate, paymentDate));
    }

    /**
     * @param amount   D: cuota exigible (interés + capital)
     * @param interest interés compensatorio incluido en D
     * @param lateRate tasa moratoria; solo se usa si hay días de atraso (obligatoria en ese caso)
     */
    public static PaymentQuote quote(BigDecimal amount, BigDecimal interest, LocalDate dueDate, LocalDate paymentDate, EffectiveRate lateRate) {
        int days = lateDays(dueDate, paymentDate);
        var d = Rounding.money(amount);
        var compensatory = Rounding.money(interest);
        if (days == 0) {
            var zero = Rounding.money(BigDecimal.ZERO);
            return new PaymentQuote(d, 0, null, BigDecimal.ZERO.setScale(Rounding.RATE_SCALE), zero, d, zero, compensatory,
                    Rounding.money(d.subtract(compensatory)));
        }
        if (lateRate == null) throw new IllegalArgumentException("Ingresa la tasa moratoria: el pago está vencido.");
        var ted = lateRate.toDaily();
        var factor = BigDecimal.ONE.add(ted).pow(days, MathContext.DECIMAL128).subtract(BigDecimal.ONE);
        var late = Rounding.money(d.multiply(factor));
        var total = Rounding.money(d.add(late));
        return new PaymentQuote(d, days, lateRate, ted, late, total, late, compensatory, Rounding.money(d.subtract(compensatory)));
    }

    /** No se aceptan pagos parciales: el monto debe ser igual al total exigible. */
    public static boolean isFullPayment(BigDecimal paid, BigDecimal total) {
        return paid != null && Rounding.money(paid).compareTo(Rounding.money(total)) == 0;
    }
}
