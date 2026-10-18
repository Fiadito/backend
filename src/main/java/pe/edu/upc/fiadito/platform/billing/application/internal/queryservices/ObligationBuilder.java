package pe.edu.upc.fiadito.platform.billing.application.internal.queryservices;

import pe.edu.upc.fiadito.platform.billing.domain.model.aggregates.Payment;
import pe.edu.upc.fiadito.platform.billing.domain.model.readmodels.Obligation;
import pe.edu.upc.fiadito.platform.credit.domain.model.aggregates.CreditSale;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Arma las obligaciones (cuotas y pagos únicos) de un conjunto de ventas con sus pagos. */
final class ObligationBuilder {

    private ObligationBuilder() {
    }

    static List<Obligation> build(List<CreditSale> sales, List<Payment> payments) {
        var result = new ArrayList<Obligation>();
        for (var sale : sales) {
            if (sale.isInInstallments()) {
                sale.getInstallments().forEach(installment -> result.add(new Obligation(sale, installment,
                        payments.stream().filter(p -> installment.getId().equals(p.getInstallmentId())).findFirst().orElse(null))));
            } else {
                result.add(new Obligation(sale, null,
                        payments.stream().filter(p -> p.getSaleId().equals(sale.getId()) && p.getInstallmentId() == null).findFirst().orElse(null)));
            }
        }
        result.sort(Comparator.comparing(Obligation::dueDate).thenComparing(o -> o.sale().getId())
                .thenComparing(o -> o.installment() == null ? 0 : o.installment().getNumber()));
        return result;
    }
}
