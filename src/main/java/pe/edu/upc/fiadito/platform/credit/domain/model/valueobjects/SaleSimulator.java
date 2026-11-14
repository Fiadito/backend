package pe.edu.upc.fiadito.platform.credit.domain.model.valueobjects;

import pe.edu.upc.fiadito.platform.catalog.domain.model.aggregates.Product;
import pe.edu.upc.fiadito.platform.membership.domain.model.aggregates.Member;
import pe.edu.upc.fiadito.platform.shared.domain.model.valueobjects.EffectiveRate;
import pe.edu.upc.fiadito.platform.shared.domain.model.valueobjects.RatePeriod;
import pe.edu.upc.fiadito.platform.shared.domain.model.valueobjects.Rounding;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio de dominio: valida la compra y calcula el pago único o el plan de pagos con interés (módulo 7.6).
 * La tasa efectiva se elige en cada venta; su TEQ es la tasa de cada cuota quincenal.
 */
public final class SaleSimulator {

    private SaleSimulator() {
    }

    public static SaleSimulation simulate(SaleRequest request, Member member, Product product, BigDecimal memberDebt, LocalDate today) {
        var errors = new ArrayList<String>();
        if (member == null) errors.add("Selecciona un socio.");
        if (product == null) errors.add("Selecciona un producto o combo.");
        if (request.quantity() == null || request.quantity() < 1) errors.add("La cantidad debe ser un número entero mayor o igual a 1.");
        if (request.purchaseDateTime() == null) errors.add("Ingresa la fecha y hora de la compra.");
        if (request.paymentMode() == null) errors.add("Selecciona la modalidad de pago.");
        var rateError = CreditPolicy.saleRateError(request.ratePeriod(), request.rateValue());
        if (!errors.isEmpty()) {
            if (rateError != null) errors.add(rateError);
            return new SaleSimulation(errors, null, null, request.paymentMode(), 0, null, null, request.ratePeriod(), request.rateValue(),
                    null, null, null, null, null, List.of(), memberDebt, null);
        }

        var amount = Rounding.money(product.getListPrice().multiply(BigDecimal.valueOf(request.quantity())));
        var inInstallments = request.paymentMode() == PaymentMode.CUOTAS;
        int count = inInstallments ? (request.installmentCount() == null ? 0 : request.installmentCount()) : 1;
        var available = Rounding.money(member.getCreditLimit().subtract(memberDebt));

        if (!member.isActive()) errors.add("El socio está dado de baja.");
        if (!product.isActive()) errors.add("El producto está dado de baja.");
        if (request.purchaseDateTime().toLocalDate().isAfter(today)) errors.add("La fecha de compra no puede ser futura.");
        if (!CreditPolicy.isAmountInRange(amount))
            errors.add("El monto debe estar entre S/ 400.00 y S/ 1,000.00 (precio de lista × cantidad = S/ " + amount + ").");
        if (memberDebt.add(amount).compareTo(member.getCreditLimit()) > 0)
            errors.add("La compra excede el límite de crédito del socio (crédito insuficiente).");
        if (!product.getSaleMode().allows(request.paymentMode())) errors.add("El producto no se vende en esta modalidad.");
        if (inInstallments && (count < 1 || count > member.getMaxInstallments()))
            errors.add("Número de cuotas no permitido (de 1 a " + member.getMaxInstallments() + ").");
        if (rateError != null) errors.add(rateError);

        var paymentDate = PaymentCalendar.firstPaymentDate(request.purchaseDateTime());
        var cutoff = PaymentCalendar.cutoffOf(request.purchaseDateTime());
        if (rateError != null) {
            return new SaleSimulation(errors, product.getListPrice(), amount, request.paymentMode(), count, cutoff, paymentDate,
                    request.ratePeriod(), request.rateValue(), null, null, null, null, null, List.of(), memberDebt, available);
        }

        var rate = new EffectiveRate(request.ratePeriod(), request.rateValue());
        var periodRate = rate.toPeriod(RatePeriod.TEQ);
        var validCount = count >= 1 && count <= CreditPolicy.MAX_INSTALLMENTS;
        List<ScheduleRow> schedule = List.of();
        BigDecimal installment = null, interest = null, total = null;
        if (inInstallments && validCount) {
            schedule = InstallmentSchedule.build(amount, periodRate, paymentDate, count);
            installment = schedule.getFirst().amount();
            interest = schedule.stream().map(ScheduleRow::interest).reduce(BigDecimal.ZERO, BigDecimal::add);
            total = schedule.stream().map(ScheduleRow::amount).reduce(BigDecimal.ZERO, BigDecimal::add);
        } else if (!inInstallments) {
            interest = InstallmentSchedule.singlePaymentInterest(amount, periodRate);
            total = Rounding.money(amount.add(interest));
            installment = total;
        }
        return new SaleSimulation(errors, product.getListPrice(), amount, request.paymentMode(), count, cutoff, paymentDate,
                rate.period(), rate.value(), periodRate, rate.toAnnual(), installment, interest, total, schedule, memberDebt, available);
    }
}
