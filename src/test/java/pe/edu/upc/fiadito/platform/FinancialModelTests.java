package pe.edu.upc.fiadito.platform;

import org.junit.jupiter.api.Test;
import pe.edu.upc.fiadito.platform.billing.domain.model.valueobjects.LateInterest;
import pe.edu.upc.fiadito.platform.credit.domain.model.valueobjects.CreditPolicy;
import pe.edu.upc.fiadito.platform.credit.domain.model.valueobjects.InstallmentSchedule;
import pe.edu.upc.fiadito.platform.credit.domain.model.valueobjects.PaymentCalendar;
import pe.edu.upc.fiadito.platform.credit.domain.model.valueobjects.ScheduleRow;
import pe.edu.upc.fiadito.platform.shared.domain.model.valueobjects.EffectiveRate;
import pe.edu.upc.fiadito.platform.shared.domain.model.valueobjects.RatePeriod;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pruebas del cálculo financiero (método francés quincenal con la tasa de la venta y mora solo si está vencido).
 * Los valores esperados se calcularon aparte con Python (decimal, mismas reglas de redondeo).
 */
class FinancialModelTests {

    private static final BigDecimal P500 = new BigDecimal("500.00");
    private static final EffectiveRate TEA20 = new EffectiveRate(RatePeriod.TEA, new BigDecimal("0.20"));

    private static BigDecimal sum(java.util.List<ScheduleRow> rows, java.util.function.Function<ScheduleRow, BigDecimal> f) {
        return rows.stream().map(f).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Test
    void conversionDeTasasCon7DecimalesEnPorcentaje() {
        var tea60 = new EffectiveRate(RatePeriod.TEA, new BigDecimal("0.60"));
        assertThat(tea60.toDaily()).isEqualByComparingTo("0.001306418");          // 0.1306418 %
        assertThat(tea60.toPeriod(RatePeriod.TEM)).isEqualByComparingTo("0.039944108");
        var teq40 = new EffectiveRate(RatePeriod.TEQ, new BigDecimal("0.40"));
        assertThat(teq40.toDaily()).isEqualByComparingTo("0.022684960");          // 2.2684960 %
        assertThat(TEA20.toPeriod(RatePeriod.TEQ)).isEqualByComparingTo("0.007625660");
    }

    @Test
    void rangoDeLaTasaSeValidaSobreLaTeaEquivalente() {
        assertThat(CreditPolicy.saleRateError(RatePeriod.TEA, new BigDecimal("0.15"))).isNull();
        assertThat(CreditPolicy.saleRateError(RatePeriod.TEA, new BigDecimal("0.27"))).isNull();
        assertThat(CreditPolicy.saleRateError(RatePeriod.TEA, new BigDecimal("0.60"))).isNotNull();
        assertThat(CreditPolicy.saleRateError(RatePeriod.TEQ, new BigDecimal("0.01"))).isNull();     // TEA 26.97 %
        assertThat(CreditPolicy.saleRateError(RatePeriod.TEM, new BigDecimal("0.025"))).isNotNull(); // TEA 34.49 %
        // Moratoria: una TEQ realista ahora se acepta (antes se exigía 40 % sobre el número escrito, sin importar el periodo)
        assertThat(CreditPolicy.lateRateError(new EffectiveRate(RatePeriod.TEQ, new BigDecimal("0.02")))).isNull();     // TEA 60.84 %
        assertThat(CreditPolicy.lateRateError(new EffectiveRate(RatePeriod.TEQ, new BigDecimal("0.01")))).isNotNull();  // TEA 26.97 %
        assertThat(CreditPolicy.lateRateError(new EffectiveRate(RatePeriod.TEA, new BigDecimal("0.60")))).isNull();
        assertThat(CreditPolicy.lateRateError(new EffectiveRate(RatePeriod.TEA, new BigDecimal("0.30")))).isNotNull();
    }

    @Test
    void juego1_ochoCuotasFrancesTea20() {
        var first = PaymentCalendar.firstPaymentDate(LocalDateTime.of(2026, 10, 5, 10, 0));
        var teq = TEA20.toPeriod(RatePeriod.TEQ);
        var rows = InstallmentSchedule.build(P500, teq, first, 8);
        assertThat(first).isEqualTo(LocalDate.of(2026, 10, 15));
        assertThat(InstallmentSchedule.fixedInstallment(P500, teq, 8)).isEqualByComparingTo("64.66");
        assertThat(rows.getFirst().interest()).isEqualByComparingTo("3.81");
        assertThat(rows.getFirst().amortization()).isEqualByComparingTo("60.85");
        assertThat(rows.get(1).openingBalance()).isEqualByComparingTo("439.15");
        assertThat(rows.get(1).interest()).isEqualByComparingTo("3.35");
        assertThat(rows.get(7).amount()).isEqualByComparingTo("64.70");
        assertThat(rows.get(7).closingBalance()).isEqualByComparingTo("0.00");
        assertThat(rows.get(7).dueDate()).isEqualTo(LocalDate.of(2027, 1, 30));
        assertThat(sum(rows, ScheduleRow::amortization)).isEqualByComparingTo("500.00");
        assertThat(sum(rows, ScheduleRow::interest)).isEqualByComparingTo("17.32");
        assertThat(sum(rows, ScheduleRow::amount)).isEqualByComparingTo("517.32");
    }

    @Test
    void juego2_moraDeCincoDiasSoloPorqueEstaVencida() {
        var tea60 = new EffectiveRate(RatePeriod.TEA, new BigDecimal("0.60"));
        var due = LocalDate.of(2026, 10, 30);
        var late = LateInterest.quote(new BigDecimal("64.66"), new BigDecimal("3.35"), due, LocalDate.of(2026, 11, 4), tea60);
        assertThat(late.lateDays()).isEqualTo(5);
        assertThat(late.dailyRate()).isEqualByComparingTo("0.001306418");
        assertThat(late.lateInterest()).isEqualByComparingTo("0.42");
        assertThat(late.total()).isEqualByComparingTo("65.08");
        assertThat(late.lateImputation()).isEqualByComparingTo("0.42");
        assertThat(late.interestImputation()).isEqualByComparingTo("3.35");
        assertThat(late.capitalImputation()).isEqualByComparingTo("61.31");
        assertThat(LateInterest.isFullPayment(new BigDecimal("64.66"), late.total())).isFalse();

        var onTime = LateInterest.quote(new BigDecimal("64.66"), new BigDecimal("3.35"), due, due, null);
        assertThat(onTime.isLate()).isFalse();
        assertThat(onTime.lateRate()).isNull();
        assertThat(onTime.lateInterest()).isEqualByComparingTo("0.00");
        assertThat(onTime.total()).isEqualByComparingTo("64.66");
    }

    @Test
    void juego3_compraPosteriorAlCorte() {
        var first = PaymentCalendar.firstPaymentDate(LocalDateTime.of(2026, 10, 26, 18, 0));
        var rows = InstallmentSchedule.build(new BigDecimal("450.00"), TEA20.toPeriod(RatePeriod.TEQ), first, 8);
        assertThat(first).isEqualTo(LocalDate.of(2026, 11, 15));
        assertThat(rows.getFirst().amount()).isEqualByComparingTo("58.20");
        assertThat(rows.get(7).amount()).isEqualByComparingTo("58.17");
        assertThat(rows.get(7).dueDate()).isEqualTo(LocalDate.of(2027, 2, 28));
    }

    @Test
    void pagoUnicoLlevaUnaQuincenaDeInteres() {
        var teq = new EffectiveRate(RatePeriod.TEA, new BigDecimal("0.18")).toPeriod(RatePeriod.TEQ);
        assertThat(teq).isEqualByComparingTo("0.006920270");
        assertThat(InstallmentSchedule.singlePaymentInterest(new BigDecimal("420.00"), teq)).isEqualByComparingTo("2.91");
    }

    @Test
    void reglasDeCorteYRedondeo() {
        assertThat(PaymentCalendar.firstPaymentDate(LocalDateTime.of(2026, 10, 10, 23, 59))).isEqualTo(LocalDate.of(2026, 10, 15));
        assertThat(PaymentCalendar.firstPaymentDate(LocalDateTime.of(2026, 10, 11, 8, 0))).isEqualTo(LocalDate.of(2026, 10, 30));
        assertThat(PaymentCalendar.paymentDateOfCutoff(LocalDate.of(2026, 10, 25))).isEqualTo(LocalDate.of(2026, 10, 30));
        var rows = InstallmentSchedule.build(new BigDecimal("1000.00"), TEA20.toPeriod(RatePeriod.TEQ), LocalDate.of(2026, 10, 15), 3);
        assertThat(rows.getFirst().amount()).isEqualByComparingTo("338.43");
        assertThat(rows.get(1).amortization()).isEqualByComparingTo("333.33");
        assertThat(rows.get(2).amortization()).isEqualByComparingTo("335.87");
    }
}
