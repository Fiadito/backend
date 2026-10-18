package pe.edu.upc.fiadito.platform.billing.application.internal.commandservices;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.fiadito.platform.billing.domain.model.aggregates.Payment;
import pe.edu.upc.fiadito.platform.billing.domain.model.commands.QuotePaymentCommand;
import pe.edu.upc.fiadito.platform.billing.domain.model.commands.RegisterPaymentCommand;
import pe.edu.upc.fiadito.platform.billing.domain.model.readmodels.Obligation;
import pe.edu.upc.fiadito.platform.billing.domain.model.valueobjects.LateInterest;
import pe.edu.upc.fiadito.platform.billing.domain.services.PaymentCommandService;
import pe.edu.upc.fiadito.platform.billing.infrastructure.persistence.jpa.repositories.PaymentRepository;
import pe.edu.upc.fiadito.platform.credit.interfaces.acl.CreditContextFacade;
import pe.edu.upc.fiadito.platform.membership.interfaces.acl.MembershipContextFacade;
import pe.edu.upc.fiadito.platform.shared.application.time.SystemDateProvider;
import pe.edu.upc.fiadito.platform.shared.domain.exceptions.BusinessRuleException;
import pe.edu.upc.fiadito.platform.shared.domain.model.events.OperationPerformedEvent;

/**
 * RegistrarPago (módulo 7.9): calcula la mora, rechaza pagos parciales, imputa mora → capital y marca la obligación pagada.
 * PAGO, el estado de CUOTA/VENTA y BITACORA se guardan en una sola transacción.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class PaymentCommandServiceImpl implements PaymentCommandService {

    private final PaymentRepository repository;
    private final CreditContextFacade credit;
    private final MembershipContextFacade membership;
    private final SystemDateProvider dates;
    private final ApplicationEventPublisher events;

    @Override
    @Transactional(readOnly = true)
    public QuotedObligation handle(QuotePaymentCommand c) {
        var obligation = findObligation(c.businessId(), c.saleId(), c.installmentId());
        if (c.paymentDate() == null) throw new BusinessRuleException("Ingresa la fecha real de pago.");
        var member = membership.findMember(c.businessId(), obligation.memberId()).orElseThrow();
        return new QuotedObligation(obligation, LateInterest.quote(obligation.amount(), obligation.dueDate(), c.paymentDate(), member.lateRate()));
    }

    @Override
    public Payment handle(RegisterPaymentCommand c, Long actorUserId, String actorName) {
        if (c.method() == null) throw new BusinessRuleException("Selecciona el medio de pago.");
        var quoted = handle(new QuotePaymentCommand(c.businessId(), c.saleId(), c.installmentId(), c.paymentDate()));
        var obligation = quoted.obligation();
        if (obligation.isPaid()) throw new BusinessRuleException("Esta obligación ya fue pagada.");
        if (c.paymentDate().isBefore(obligation.sale().getPurchaseDateTime().toLocalDate()))
            throw new BusinessRuleException("La fecha de pago no puede ser anterior a la fecha de compra.");
        if (!LateInterest.isFullPayment(c.paid(), quoted.quote().total()))
            throw new BusinessRuleException("Debe cancelar el total: S/ " + quoted.quote().total() + " (no se aceptan pagos parciales).");

        var payment = repository.save(new Payment(c.businessId(), obligation.sale().getId(),
                obligation.installment() == null ? null : obligation.installment().getId(), obligation.memberId(), c.paymentDate(),
                quoted.quote(), c.method(), dates.now()));
        credit.markPaid(c.businessId(), obligation.sale().getId(), payment.getInstallmentId());
        var member = membership.findMember(c.businessId(), obligation.memberId()).orElseThrow();
        events.publishEvent(new OperationPerformedEvent(c.businessId(), actorUserId, actorName, "PAGAR", "PAGO", payment.getId(),
                "Pago de " + member.getFullName() + " – " + obligation.label() + ": S/ " + payment.getTotalPaid()
                        + " (mora S/ " + payment.getLateInterest() + ", " + payment.getLateDays() + " días)"));
        return payment;
    }

    private Obligation findObligation(Long businessId, Long saleId, Long installmentId) {
        if (saleId == null) throw new BusinessRuleException("Selecciona una obligación pendiente.");
        var sale = credit.findSale(businessId, saleId);
        if (sale.isInInstallments()) {
            if (installmentId == null) throw new BusinessRuleException("Indica la cuota que se paga.");
            var installment = sale.installment(installmentId);
            return new Obligation(sale, installment, null);
        }
        return new Obligation(sale, null, null);
    }
}
