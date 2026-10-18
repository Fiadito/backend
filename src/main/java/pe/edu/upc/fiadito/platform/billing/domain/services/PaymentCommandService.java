package pe.edu.upc.fiadito.platform.billing.domain.services;

import pe.edu.upc.fiadito.platform.billing.domain.model.aggregates.Payment;
import pe.edu.upc.fiadito.platform.billing.domain.model.commands.QuotePaymentCommand;
import pe.edu.upc.fiadito.platform.billing.domain.model.commands.RegisterPaymentCommand;
import pe.edu.upc.fiadito.platform.billing.domain.model.readmodels.Obligation;
import pe.edu.upc.fiadito.platform.billing.domain.model.valueobjects.PaymentQuote;

public interface PaymentCommandService {
    /** Cotización: devuelve la obligación y su total exigible a la fecha indicada. */
    QuotedObligation handle(QuotePaymentCommand command);

    Payment handle(RegisterPaymentCommand command, Long actorUserId, String actorName);

    record QuotedObligation(Obligation obligation, PaymentQuote quote) {
    }
}
