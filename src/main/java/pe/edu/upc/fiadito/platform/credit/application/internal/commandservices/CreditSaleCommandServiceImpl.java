package pe.edu.upc.fiadito.platform.credit.application.internal.commandservices;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.fiadito.platform.catalog.interfaces.acl.CatalogContextFacade;
import pe.edu.upc.fiadito.platform.credit.domain.model.aggregates.CreditSale;
import pe.edu.upc.fiadito.platform.credit.domain.model.commands.RegisterSaleCommand;
import pe.edu.upc.fiadito.platform.credit.domain.model.commands.SimulateSaleCommand;
import pe.edu.upc.fiadito.platform.credit.domain.model.valueobjects.SaleSimulation;
import pe.edu.upc.fiadito.platform.credit.domain.model.valueobjects.SaleSimulator;
import pe.edu.upc.fiadito.platform.credit.domain.services.CreditSaleCommandService;
import pe.edu.upc.fiadito.platform.credit.infrastructure.persistence.jpa.repositories.CreditSaleRepository;
import pe.edu.upc.fiadito.platform.membership.interfaces.acl.MembershipContextFacade;
import pe.edu.upc.fiadito.platform.shared.application.time.SystemDateProvider;
import pe.edu.upc.fiadito.platform.shared.domain.exceptions.BusinessRuleException;
import pe.edu.upc.fiadito.platform.shared.domain.model.events.OperationPerformedEvent;

import java.math.BigDecimal;

/**
 * RegistrarVenta (módulo 7.6) y GenerarCronograma (7.7). La venta y sus cuotas se guardan en una sola transacción.
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
        var simulation = handle(new SimulateSaleCommand(c.businessId(), c.request()));
        if (!simulation.isValid()) throw new BusinessRuleException(simulation.errors().getFirst());
        var r = c.request();
        var sale = repository.save(new CreditSale(c.businessId(), r.memberId(), r.productId(), r.quantity(), r.purchaseDateTime(), simulation));
        var member = membership.findMember(c.businessId(), r.memberId()).orElseThrow();
        var product = catalog.findProduct(c.businessId(), r.productId()).orElseThrow();
        events.publishEvent(new OperationPerformedEvent(c.businessId(), actorUserId, actorName, "VENDER", "VENTA", sale.getId(),
                "Venta a " + member.getFullName() + ": " + sale.getQuantity() + " × " + product.getDescription() + " = S/ " + sale.getAmount()
                        + (sale.isInInstallments() ? " (" + sale.getInstallmentCount() + " cuotas)" : " (pago único)")));
        return sale;
    }

    private BigDecimal pendingCapitalOf(Long memberId) {
        return repository.findAllByMemberId(memberId).stream().map(CreditSale::pendingCapital).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
