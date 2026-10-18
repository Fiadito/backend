package pe.edu.upc.fiadito.platform.billing.domain.model.readmodels;

import pe.edu.upc.fiadito.platform.billing.domain.model.valueobjects.PaymentQuote;
import pe.edu.upc.fiadito.platform.credit.domain.model.valueobjects.DebtStatus;

/** Obligación pendiente con su mora calculada a hoy. */
public record PendingObligation(Obligation obligation, PaymentQuote quoteToday, DebtStatus status) {
}
