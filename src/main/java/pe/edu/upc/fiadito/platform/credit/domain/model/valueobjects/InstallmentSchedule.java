package pe.edu.upc.fiadito.platform.credit.domain.model.valueobjects;

import pe.edu.upc.fiadito.platform.shared.domain.model.valueobjects.Rounding;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio de dominio: plan de pagos en cuotas quincenales iguales (módulo 7.7, GenerarCronograma).
 * Sin interés compensatorio: C = P / n; A_k = C_k; SF_k = SI_k − A_k; SI_k+1 = SF_k.
 * La última cuota amortiza el saldo pendiente (A_n = SI_n) y absorbe el redondeo.
 */
public final class InstallmentSchedule {

    private InstallmentSchedule() {
    }

    /** Cuota fija C = P / n, redondeada a 2 decimales. */
    public static BigDecimal fixedInstallment(BigDecimal principal, int count) {
        return principal.divide(BigDecimal.valueOf(count), Rounding.MONEY_SCALE, RoundingMode.HALF_UP);
    }

    public static List<ScheduleRow> build(BigDecimal principal, LocalDate firstPaymentDate, int count) {
        var fixed = fixedInstallment(principal, count);
        var rows = new ArrayList<ScheduleRow>();
        var balance = Rounding.money(principal);
        var dueDate = firstPaymentDate;
        for (int k = 1; k <= count; k++) {
            if (k > 1) dueDate = PaymentCalendar.nextPaymentDate(dueDate);
            var amortization = k == count ? balance : fixed;
            var closing = Rounding.money(balance.subtract(amortization));
            rows.add(new ScheduleRow(k, dueDate, balance, amortization, amortization, closing));
            balance = closing;
        }
        return rows;
    }
}
