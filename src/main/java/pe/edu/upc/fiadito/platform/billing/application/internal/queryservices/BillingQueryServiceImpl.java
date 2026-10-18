package pe.edu.upc.fiadito.platform.billing.application.internal.queryservices;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.fiadito.platform.billing.domain.model.aggregates.Payment;
import pe.edu.upc.fiadito.platform.billing.domain.model.readmodels.AccountStatement;
import pe.edu.upc.fiadito.platform.billing.domain.model.readmodels.CutoffReport;
import pe.edu.upc.fiadito.platform.billing.domain.model.readmodels.Obligation;
import pe.edu.upc.fiadito.platform.billing.domain.services.BillingQueryService;
import pe.edu.upc.fiadito.platform.billing.infrastructure.persistence.jpa.repositories.PaymentRepository;
import pe.edu.upc.fiadito.platform.credit.interfaces.acl.CreditContextFacade;
import pe.edu.upc.fiadito.platform.membership.interfaces.acl.MembershipContextFacade;
import pe.edu.upc.fiadito.platform.shared.application.time.SystemDateProvider;
import pe.edu.upc.fiadito.platform.shared.domain.exceptions.ResourceNotFoundException;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BillingQueryServiceImpl implements BillingQueryService {

    private final PaymentRepository payments;
    private final CreditContextFacade credit;
    private final MembershipContextFacade membership;
    private final SystemDateProvider dates;

    @Override
    public List<Obligation> getObligations(Long businessId) {
        return ObligationBuilder.build(credit.salesOf(businessId), payments.findAllByBusinessIdOrderByPaymentDateDescIdDesc(businessId));
    }

    @Override
    public List<Payment> getPayments(Long businessId) {
        return payments.findAllByBusinessIdOrderByPaymentDateDescIdDesc(businessId);
    }

    @Override
    public AccountStatement getAccountStatement(Long businessId, Long memberId) {
        var member = membership.findMember(businessId, memberId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el socio " + memberId + "."));
        var sales = credit.salesOfMember(businessId, memberId);
        var memberPayments = payments.findAllByBusinessIdAndMemberIdOrderByPaymentDateDescIdDesc(businessId, memberId);
        return new AccountStatement(member, sales, ObligationBuilder.build(sales, memberPayments), memberPayments, dates.today());
    }

    @Override
    public CutoffReport getCutoffReport(Long businessId, LocalDate cutoffDate) {
        return CutoffReport.build(cutoffDate, getObligations(businessId), membership.membersOf(businessId), dates.today());
    }
}
