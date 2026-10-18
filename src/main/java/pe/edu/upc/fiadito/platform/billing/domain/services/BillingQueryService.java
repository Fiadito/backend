package pe.edu.upc.fiadito.platform.billing.domain.services;

import pe.edu.upc.fiadito.platform.billing.domain.model.aggregates.Payment;
import pe.edu.upc.fiadito.platform.billing.domain.model.readmodels.AccountStatement;
import pe.edu.upc.fiadito.platform.billing.domain.model.readmodels.CutoffReport;
import pe.edu.upc.fiadito.platform.billing.domain.model.readmodels.Obligation;

import java.time.LocalDate;
import java.util.List;

public interface BillingQueryService {
    /** Todas las obligaciones del negocio, ordenadas por fecha de pago. */
    List<Obligation> getObligations(Long businessId);

    List<Payment> getPayments(Long businessId);

    AccountStatement getAccountStatement(Long businessId, Long memberId);

    CutoffReport getCutoffReport(Long businessId, LocalDate cutoffDate);
}
