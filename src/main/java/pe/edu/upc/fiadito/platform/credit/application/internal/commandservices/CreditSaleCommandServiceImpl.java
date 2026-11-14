package pe.edu.upc.fiadito.platform.credit.application.internal.commandservices;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.fiadito.platform.catalog.interfaces.acl.CatalogContextFacade;
import pe.edu.upc.fiadito.platform.credit.domain.model.aggregates.CreditSale;
import pe.edu.upc.fiadito.platform.credit.domain.model.commands.ChangeSaleRateCommand;
import pe.edu.upc.fiadito.platform.credit.domain.model.commands.RegisterSaleCommand;
import pe.edu.upc.fiadito.platform.credit.domain.model.commands.SimulateSaleCommand;
import pe.edu.upc.fiadito.platform.credit.domain.model.valueobjects.CreditPolicy;
import pe.edu.upc.fiadito.platform.credit.domain.model.valueobjects.SaleSimulation;
import pe.edu.upc.fiadito.platform.credit.domain.model.valueobjects.SaleSimulator;
import pe.edu.upc.fiadito.platform.credit.domain.services.CreditSaleCommandService;
import pe.edu.upc.fiadito.platform.credit.infrastructure.persistence.jpa.repositories.CreditSaleRepository;
import pe.edu.upc.fiadito.platform.membership.interfaces.acl.MembershipContextFacade;
import pe.edu.upc.fiadito.platform.shared.application.time.SystemDateProvider;
import pe.edu.upc.fiadito.platform.shared.domain.exceptions.BusinessRuleException;
import pe.edu.upc.fiadito.platform.shared.domain.exceptions.ResourceNotFoundException;
import pe.edu.upc.fiadito.platform.shared.domain.model.events.OperationPerformedEvent;
import pe.edu.upc.fiadito.platform.shared.domain.model.valueobjects.EffectiveRate;

import java.math.BigDecimal;

/**
 * RegistrarVenta (módulo 7.6), GenerarCronograma (7.7) y CambiarTasa. La venta y sus cuotas se guardan en una sola transacción.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class CreditSaleCommandServiceImpl implements CreditSaleCommandService {

    private final CreditSaleRepository repository;
    private final MembershipContextFacade membership;
    private final CatalogContextFacade catalog;
    private final SystemDateProvider dates;
    private final ApplicationEventPublisher events;

    @Override
    @Transactional(readOnly = true)
    public SaleSimulation handle(SimulateSaleCommand c) {
        var r = c.request();
        var member = r.memberId() == null ? null : membership.findMember(c.businessId(), r.memberId()).orElse(null);
        var product = r.productId() == null ? null : catalog.findProduct(c.businessId(), r.productId()).orElse(null);
        var debt = member == null ? BigDecimal.ZERO : pendingCapitalOf(member.getId());
        return SaleSimulator.simulate(r, member, product, debt, dates.today());
    }

    @Override
    public CreditSale handle(RegisterSaleCommand c, Long actorUserId, String actorName) {
        var r = c.request();
        // Bloquea al socio: dos ventas simultáneas no pueden pasar juntas el límite de crédito.
        if (r.memberId() != null) membership.lockMember(c.businessId(), r.memberId());
        var simulation = handle(new SimulateSaleCommand(c.businessId(), r));
        if (!simulation.isValid()) throw new BusinessRuleException(simulation.errors().getFirst());
        var sale = repository.save(new CreditSale(c.businessId(), r.memberId(), r.productId(), r.quantity(), r.purchaseDateTime(), simulation));
        var member = membership.findMember(c.businessId(), r.memberId()).orElseThrow();
        var product = catalog.findProduct(c.businessId(), r.productId()).orElseThrow();
        events.publishEvent(new OperationPerformedEvent(c.businessId(), actorUserId, actorName, "VENDER", "VENTA", sale.getId(),
                "Venta a " + member.getFullName() + ": " + sale.getQuantity() + " × " + product.getDescription() + " = S/ " + sale.getAmount()
                        + (sale.isInInstallments() ? " (" + sale.getInstallmentCount() + " cuotas)" : " (pago único)")
                        + ", " + sale.getRatePeriod() + " " + percent(sale.getRateValue())));
        return sale;
    }

    @Override
    public CreditSale handle(ChangeSaleRateCommand c, Long actorUserId, String actorName) {
        var error = CreditPolicy.saleRateError(c.ratePeriod(), c.rateValue());
        if (error != null) throw new BusinessRuleException(error);
        var sale = repository.findForUpdate(c.saleId(), c.businessId())
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró la venta " + c.saleId() + "."));
        var before = sale.getRatePeriod() + " " + percent(sale.getRateValue());
        sale.changeRate(new EffectiveRate(c.ratePeriod(), c.rateValue()));
        var saved = repository.save(sale);
        events.publishEvent(new OperationPerformedEvent(c.businessId(), actorUserId, actorName, "EDITAR", "VENTA", sale.getId(),
                "Cambio de tasa de la venta " + sale.getId() + ": " + before + " → " + sale.getRatePeriod() + " " + percent(sale.getRateValue())
                        + "; nueva cuota S/ " + sale.getInstallmentAmount()));
        return saved;
    }

    private BigDecimal pendingCapitalOf(Long memberId) {
        return repository.findAllByMemberId(memberId).stream().map(CreditSale::pendingCapital).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static String percent(BigDecimal fraction) {
        return fraction.movePointRight(2).stripTrailingZeros().toPlainString() + " %";
    }
}
