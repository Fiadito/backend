package pe.edu.upc.fiadito.platform.credit.domain.model.valueobjects;

import pe.edu.upc.fiadito.platform.shared.domain.model.valueobjects.Rounding;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio de dominio: plan de pagos en cuotas quincenales con interés compensatorio (método francés, cuota fija).
 * i = TEQ (tasa efectiva quincenal de la venta); C = P · i / (1 − (1 + i)^−n).
 * Por cuota: I_k = SI_k · i; A_k = C − I_k; SF_k = SI_k − A_k; SI_k+1 = SF_k.
 * La última cuota amortiza el saldo pendiente (A_n = SI_n) y absorbe el redondeo.
 */
public final class InstallmentSchedule {

    private static final MathContext MC = MathContext.DECIMAL128;

    private InstallmentSchedule() {
    }

    /** Cuota fija del método francés, redondeada a 2 decimales (si i = 0, C = P / n). */
    public static BigDecimal fixedInstallment(BigDecimal principal, BigDecimal periodRate, int count) {
        if (periodRate.signum() == 0) return principal.divide(BigDecimal.valueOf(count), Rounding.MONEY_SCALE, RoundingMode.HALF_UP);
        var factor = BigDecimal.ONE.add(periodRate).pow(count, MC);
        var annuity = periodRate.multiply(factor, MC).divide(factor.subtract(BigDecimal.ONE), MC);
        return Rounding.money(principal.multiply(annuity, MC));
    }

    /** Fechas de vencimiento: la primera fecha de pago y las siguientes quincenas (15 y 30 / fin de mes). */
    public static List<LocalDate> dueDates(LocalDate firstPaymentDate, int count) {
        var dates = new ArrayList<LocalDate>();
        var date = firstPaymentDate;
        for (int k = 1; k <= count; k++) {
            if (k > 1) date = PaymentCalendar.nextPaymentDate(date);
            dates.add(date);
        }
        return dates;
    }

    public static List<ScheduleRow> build(BigDecimal principal, BigDecimal periodRate, LocalDate firstPaymentDate, int count) {
        return build(principal, periodRate, dueDates(firstPaymentDate, count));
    }

    /** Plan para un saldo y unas fechas dadas (también se usa al recalcular las cuotas pendientes). */
    public static List<ScheduleRow> build(BigDecimal principal, BigDecimal periodRate, List<LocalDate> dueDates) {
        int count = dueDates.size();
        var fixed = fixedInstallment(principal, periodRate, count);
        var rows = new ArrayList<ScheduleRow>();
        var balance = Rounding.money(principal);
        for (int k = 1; k <= count; k++) {
            var interest = Rounding.money(balance.multiply(periodRate));
            var amortization = k == count ? balance : fixed.subtract(interest);
            var closing = Rounding.money(balance.subtract(amortization));
            rows.add(new ScheduleRow(k, dueDates.get(k - 1), balance, interest, amortization, Rounding.money(amortization.add(interest)), closing));
            balance = closing;
        }
        return rows;
    }

    /** Interés de un pago único: un periodo quincenal sobre el monto de la compra. */
    public static BigDecimal singlePaymentInterest(BigDecimal principal, BigDecimal periodRate) {
        return Rounding.money(principal.multiply(periodRate));
    }
}
