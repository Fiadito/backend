package pe.edu.upc.fiadito.platform.billing.interfaces.rest.transform;

import pe.edu.upc.fiadito.platform.billing.domain.model.aggregates.Payment;
import pe.edu.upc.fiadito.platform.billing.domain.model.readmodels.Obligation;
import pe.edu.upc.fiadito.platform.billing.domain.model.readmodels.PendingObligation;
import pe.edu.upc.fiadito.platform.billing.interfaces.rest.resources.ObligationResource;
import pe.edu.upc.fiadito.platform.billing.interfaces.rest.resources.PaymentResource;
import pe.edu.upc.fiadito.platform.billing.interfaces.rest.resources.PendingObligationResource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Convierte obligaciones y pagos en recursos REST con los nombres del socio y del producto.
 */
public class BillingResourceAssembler {

    public static ObligationResource toResource(Obligation o, Map<Long, String> memberNames, Map<Long, String> productNames, LocalDate today) {
        var sale = o.sale();
        var installment = o.installment();
        return new ObligationResource(o.key(), o.code(), sale.getId(), installment == null ? null : installment.getId(), sale.getMemberId(),
                memberNames.getOrDefault(sale.getMemberId(), ""), sale.getProductId(), productNames.getOrDefault(sale.getProductId(), ""),
                sale.getQuantity(), sale.getPurchaseDateTime(), sale.getAmount(), o.label(),
                installment == null ? 1 : installment.getNumber(), sale.getInstallmentCount(), o.dueDate(),
                installment == null ? o.amount() : installment.getOpeningBalance(),
                installment == null ? o.amount() : installment.getAmortization(), o.amount(),
                installment == null ? BigDecimal.ZERO.setScale(2) : installment.getClosingBalance(), o.statusOn(today).name());
    }

    public static PendingObligationResource toResource(PendingObligation p, Map<Long, String> memberNames, Map<Long, String> productNames,
                                                       LocalDate today) {
        return new PendingObligationResource(toResource(p.obligation(), memberNames, productNames, today), p.quoteToday().lateDays(),
                p.quoteToday().lateInterest(), p.quoteToday().total());
    }

    public static PaymentResource toResource(Payment p, List<Obligation> obligations, Map<Long, String> memberNames, Map<Long, String> productNames) {
        var obligation = obligations.stream().filter(o -> o.sale().getId().equals(p.getSaleId())
                && (p.getInstallmentId() == null ? o.installment() == null : o.installment() != null && o.installment().getId().equals(p.getInstallmentId())))
                .findFirst().orElse(null);
        return new PaymentResource(p.getId(), p.getBusinessId(), p.getSaleId(), p.getInstallmentId(), p.getMemberId(),
                memberNames.getOrDefault(p.getMemberId(), ""),
                obligation == null ? "" : productNames.getOrDefault(obligation.sale().getProductId(), ""),
                obligation == null ? "" : obligation.label(), p.getPaymentDate(), p.getLateDays(), p.getLateDailyRate(), p.getLateInterest(),
                p.getTotalPaid(), p.getLateImputation(), p.getCapitalImputation(), p.getMethod().name(), p.getRegisteredAt());
    }
}
