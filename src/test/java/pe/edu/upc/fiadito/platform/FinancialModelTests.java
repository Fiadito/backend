package pe.edu.upc.fiadito.platform;

import org.junit.jupiter.api.Test;
import pe.edu.upc.fiadito.platform.billing.domain.model.valueobjects.LateInterest;
import pe.edu.upc.fiadito.platform.credit.domain.model.valueobjects.InstallmentSchedule;
import pe.edu.upc.fiadito.platform.credit.domain.model.valueobjects.PaymentCalendar;
import pe.edu.upc.fiadito.platform.shared.domain.model.valueobjects.EffectiveRate;
import pe.edu.upc.fiadito.platform.shared.domain.model.valueobjects.RatePeriod;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tabla 11 del informe: pruebas del cálculo financiero.
 */
class FinancialModelTests {

    private static final BigDecimal P500 = new BigDecimal("500.00");

    @Test
    void juego1_ochoCuotasDe6250Del15octAl30ene() {
        var first = PaymentCalendar.firstPaymentDate(LocalDateTime.of(2026, 10, 5, 10, 0));
        var rows = InstallmentSchedule.build(P500, first, 8);
        assertThat(first).isEqualTo(LocalDate.of(2026, 10, 15));
        assertThat(InstallmentSchedule.fixedInstallment(P500, 8)).isEqualByComparingTo("62.50");
        assertThat(rows.get(7).dueDate()).isEqualTo(LocalDate.of(2027, 1, 30));
        assertThat(rows.stream().map(r -> r.amount()).reduce(BigDecimal.ZERO, BigDecimal::add)).isEqualByComparingTo("500.00");
        assertThat(rows.get(7).closingBalance()).isEqualByComparingTo("0.00");
        assertThat(rows.get(1).dueDate()).isEqualTo(LocalDate.of(2026, 10, 30));
    }

    @Test
    void juego2_moraDeCincoDias() {
        var tea60 = new EffectiveRate(RatePeriod.TEA, new BigDecimal("0.60"));
        assertThat(tea60.toDaily()).isEqualByComparingTo("0.0013064");
        var quote = LateInterest.quote(new BigDecimal("62.50"), LocalDate.of(2026, 10, 30), LocalDate.of(2026, 11, 4), tea60);
        assertThat(quote.lateDays()).isEqualTo(5);
        assertThat(quote.lateInterest()).isEqualByComparingTo("0.41");
        assertThat(quote.total()).isEqualByComparingTo("62.91");
        assertThat(quote.lateImputation()).isEqualByComparingTo("0.41");
        assertThat(quote.capitalImputation()).isEqualByComparingTo("62.50");
        assertThat(LateInterest.isFullPayment(new BigDecimal("60"), quote.total())).isFalse();
        assertThat(tea60.toPeriod(RatePeriod.TEM)).isEqualByComparingTo("0.0399441");
    }

    @Test
    void juego3_compraPosteriorAlCorte() {
        var first = PaymentCalendar.firstPaymentDate(LocalDateTime.of(2026, 10, 26, 18, 0));
        var rows = InstallmentSchedule.build(new BigDecimal("450.00"), first, 8);
        assertThat(first).isEqualTo(LocalDate.of(2026, 11, 15));
        assertThat(rows.getFirst().amount()).isEqualByComparingTo("56.25");
        assertThat(rows.get(7).dueDate()).isEqualTo(LocalDate.of(2027, 2, 28));
    }

    @Test
    void reglasDeCorteYRedondeo() {
        assertThat(PaymentCalendar.firstPaymentDate(LocalDateTime.of(2026, 10, 10, 23, 59))).isEqualTo(LocalDate.of(2026, 10, 15));
        assertThat(PaymentCalendar.firstPaymentDate(LocalDateTime.of(2026, 10, 11, 8, 0))).isEqualTo(LocalDate.of(2026, 10, 30));
        assertThat(PaymentCalendar.paymentDateOfCutoff(LocalDate.of(2026, 10, 25))).isEqualTo(LocalDate.of(2026, 10, 30));
        var rows = InstallmentSchedule.build(new BigDecimal("1000.00"), LocalDate.of(2026, 10, 15), 3);
        assertThat(rows.get(2).amount()).isEqualByComparingTo("333.34");
    }
}
