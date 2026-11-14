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
import pe.edu.upc.fiadito.platform.credit.domain.model.aggregates.CreditSale;
import pe.edu.upc.fiadito.platform.credit.domain.model.valueobjects.CreditPolicy;
import pe.edu.upc.fiadito.platform.credit.interfaces.acl.CreditContextFacade;
import pe.edu.upc.fiadito.platform.membership.interfaces.acl.MembershipContextFacade;
import pe.edu.upc.fiadito.platform.shared.application.time.SystemDateProvider;
import pe.edu.upc.fiadito.platform.shared.domain.exceptions.BusinessRuleException;
import pe.edu.upc.fiadito.platform.shared.domain.model.events.OperationPerformedEvent;
import pe.edu.upc.fiadito.platform.shared.domain.model.valueobjects.EffectiveRate;

/**
 * RegistrarPago (módulo 7.9): calcula la mora solo si la obligación está vencida (con la tasa moratoria que se coloca al pagar),
 * rechaza pagos parciales y con fecha futura, imputa mora → interés → capital y marca la obligación pagada.
 * PAGO, el estado de CUOTA/VENTA y BITACORA se guardan en una sola transacción, con la venta bloqueada
 * para que dos pagos simultáneos de la misma obligación no pasen los dos.
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
        if (c.saleId() == null) throw new BusinessRuleException("Selecciona una obligación pendiente.");
        return quote(credit.findSale(c.businessId(), c.saleId()), c);
    }

    @Override
    public Payment handle(RegisterPaymentCommand c, Long actorUserId, String actorName) {
        if (c.method() == null) throw new BusinessRuleException("Selecciona el medio de pago.");
        if (c.saleId() == null) throw new BusinessRuleException("Selecciona una obligación pendiente.");
        var sale = credit.lockSale(c.businessId(), c.saleId());
        var quoted = quote(sale, new QuotePaymentCommand(c.businessId(), c.saleId(), c.installmentId(), c.paymentDate(),
                c.lateRatePeriod(), c.lateRateValue()));
        var obligation = quoted.obligation();
        if (obligation.isPaid()) throw new BusinessRuleException("Esta obligación ya fue pagada.");
        if (c.paymentDate().isAfter(dates.today())) throw new BusinessRuleException("La fecha de pago no puede ser futura.");
        if (c.paymentDate().isBefore(sale.getPurchaseDateTime().toLocalDate()))
            throw new BusinessRuleException("La fecha de pago no puede ser anterior a la fecha de compra.");
        if (!LateInterest.isFullPayment(c.paid(), quoted.quote().total()))
            throw new BusinessRuleException("Debe cancelar el total: S/ " + quoted.quote().total() + " (no se aceptan pagos parciales).");

        var payment = repository.save(new Payment(c.businessId(), sale.getId(),
                obligation.installment() == null ? null : obligation.installment().getId(), obligation.memberId(), c.paymentDate(),
                quoted.quote(), c.method(), dates.now()));
        credit.markPaid(c.businessId(), sale.getId(), payment.getInstallmentId());
        var member = membership.findMember(c.businessId(), obligation.memberId()).orElseThrow();
        var q = quoted.quote();
        events.publishEvent(new OperationPerformedEvent(c.businessId(), actorUserId, actorName, "PAGAR", "PAGO", payment.getId(),
                "Pago de " + member.getFullName() + " – " + obligation.label() + ": S/ " + payment.getTotalPaid()
                        + (q.isLate() ? " (mora S/ " + q.lateInterest() + ", " + q.lateDays() + " días, " + q.lateRate().period() + " "
                        + q.lateRate().value().movePointRight(2).stripTrailingZeros().toPlainString() + " %)" : " (a tiempo)")));
        return payment;
    }

    private QuotedObligation quote(CreditSale sale, QuotePaymentCommand c) {
        var obligation = obligationOf(sale, c.installmentId());
        if (c.paymentDate() == null) throw new BusinessRuleException("Ingresa la fecha real de pago.");
        EffectiveRate lateRate = null;
        if (LateInterest.lateDays(obligation.dueDate(), c.paymentDate()) > 0) {
            if (c.lateRatePeriod() == null && c.lateRateValue() == null) lateRate = CreditPolicy.DEFAULT_LATE_RATE;
            else if (c.lateRatePeriod() == null || c.lateRateValue() == null)
                throw new BusinessRuleException("Ingresa el periodo y el valor de la tasa moratoria.");
            else lateRate = new EffectiveRate(c.lateRatePeriod(), c.lateRateValue());
            var error = CreditPolicy.lateRateError(lateRate);
            if (error != null) throw new BusinessRuleException(error);
        }
        return new QuotedObligation(obligation,
                LateInterest.quote(obligation.amount(), obligation.interest(), obligation.dueDate(), c.paymentDate(), lateRate));
    }

    private static Obligation obligationOf(CreditSale sale, Long installmentId) {
        if (sale.isInInstallments()) {
            if (installmentId == null) throw new BusinessRuleException("Indica la cuota que se paga.");
            return new Obligation(sale, sale.installment(installmentId), null);
        }
        return new Obligation(sale, null, null);
    }
}
