package pe.edu.upc.fiadito.platform.credit.interfaces.rest.transform;

import pe.edu.upc.fiadito.platform.credit.domain.model.aggregates.CreditSale;
import pe.edu.upc.fiadito.platform.credit.domain.model.entities.Installment;
import pe.edu.upc.fiadito.platform.credit.interfaces.rest.resources.InstallmentResource;
import pe.edu.upc.fiadito.platform.credit.interfaces.rest.resources.SaleResource;

import java.time.LocalDate;
import java.util.Map;

public class SaleResourceAssembler {

    public static InstallmentResource toResource(Installment i, CreditSale sale, LocalDate today) {
        return new InstallmentResource(i.getId(), sale.getId(), sale.getMemberId(), i.getNumber(), sale.getInstallmentCount(), i.getDueDate(),
                i.getOpeningBalance(), i.getInterest(), i.getAmortization(), i.getAmount(), i.getClosingBalance(), i.statusOn(today).name());
    }

    public static SaleResource toResource(CreditSale s, Map<Long, String> memberNames, Map<Long, String> productNames, LocalDate today) {
        return new SaleResource(s.getId(), s.getBusinessId(), s.getMemberId(), memberNames.getOrDefault(s.getMemberId(), ""),
                s.getProductId(), productNames.getOrDefault(s.getProductId(), ""), s.getQuantity(), s.getListPrice(), s.getAmount(),
                s.getPurchaseDateTime(), s.getPaymentMode().name(), s.getInstallmentCount(), s.getRatePeriod().name(), s.getRateValue(),
                s.getPeriodRate(), s.rate().toAnnual(), s.getPaymentDate(), s.getInstallmentAmount(), s.getInterestTotal(),
                s.getTotalToPay(), s.statusOn(today).name(),
                s.getInstallments().stream().map(i -> toResource(i, s, today)).toList());
    }
}
