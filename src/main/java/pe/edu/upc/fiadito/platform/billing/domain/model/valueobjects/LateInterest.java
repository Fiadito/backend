package pe.edu.upc.fiadito.platform.billing.domain.model.valueobjects;

import pe.edu.upc.fiadito.platform.shared.domain.model.valueobjects.EffectiveRate;
import pe.edu.upc.fiadito.platform.shared.domain.model.valueobjects.Rounding;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Servicio de dominio: interés moratorio e imputación del pago (fórmulas 6 y 7, módulo 7.9).
 * dm = Freal − Fpago; Imor = D × ((1 + TED_mor)^dm − 1); Total = D + Imor; imputación: mora → capital.
 */
public final class LateInterest {

    private LateInterest() {
    }

    public static int lateDays(LocalDate dueDate, LocalDate paymentDate) {
        return (int) Math.max(0, ChronoUnit.DAYS.between(dueDate, paymentDate));
    }

    public static PaymentQuote quote(BigDecimal amount, LocalDate dueDate, LocalDate paymentDate, EffectiveRate lateRate) {
        int days = lateDays(dueDate, paymentDate);
        var ted = lateRate.toDaily();
        var interest = days > 0
                ? Rounding.money(amount.multiply(BigDecimal.valueOf(Math.pow(1 + ted.doubleValue(), days) - 1)))
                : Rounding.money(BigDecimal.ZERO);
        var total = Rounding.money(amount.add(interest));
        return new PaymentQuote(Rounding.money(amount), days, ted, interest, total, interest, Rounding.money(total.subtract(interest)));
    }

    /** No se aceptan pagos parciales: el monto debe ser igual al total exigible. */
    public static boolean isFullPayment(BigDecimal paid, BigDecimal total) {
        return paid != null && Rounding.money(paid).compareTo(Rounding.money(total)) == 0;
    }
}
