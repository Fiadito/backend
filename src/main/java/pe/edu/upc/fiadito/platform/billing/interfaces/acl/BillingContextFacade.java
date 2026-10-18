package pe.edu.upc.fiadito.platform.billing.interfaces.acl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.fiadito.platform.billing.domain.services.BillingQueryService;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Fachada del contexto Billing para la lista de socios (deuda, disponible y mora de cada uno).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BillingContextFacade {

    private final BillingQueryService queryService;

    public record DebtSummary(BigDecimal capitalDebt, BigDecimal availableCredit, boolean overdue) {
    }

    public DebtSummary debtSummaryOf(Long businessId, Long memberId) {
        var statement = queryService.getAccountStatement(businessId, memberId);
        return new DebtSummary(statement.capitalDebt(), statement.availableCredit(), statement.isOverdue());
    }

    public Map<Long, DebtSummary> debtSummaries(Long businessId, Iterable<Long> memberIds) {
        var result = new HashMap<Long, DebtSummary>();
        memberIds.forEach(id -> result.put(id, debtSummaryOf(businessId, id)));
        return result;
    }
}
