package pe.edu.upc.fiadito.platform.credit.interfaces.acl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.fiadito.platform.credit.domain.model.aggregates.CreditSale;
import pe.edu.upc.fiadito.platform.credit.infrastructure.persistence.jpa.repositories.CreditSaleRepository;
import pe.edu.upc.fiadito.platform.shared.domain.exceptions.ResourceNotFoundException;

import java.math.BigDecimal;
import java.util.List;

/**
 * Fachada del contexto Credit para Membership y Billing.
 */
@Service
@RequiredArgsConstructor
public class CreditContextFacade {

    private final CreditSaleRepository repository;

    /** DeudaCapital(socio): cuotas y pagos únicos sin pagar. */
    @Transactional(readOnly = true)
    public BigDecimal pendingCapitalOf(Long memberId) {
        return repository.findAllByMemberId(memberId).stream().map(CreditSale::pendingCapital).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transactional(readOnly = true)
    public List<CreditSale> salesOf(Long businessId) {
        return repository.findAllByBusinessIdOrderByPurchaseDateTimeAsc(businessId);
    }

    @Transactional(readOnly = true)
    public List<CreditSale> salesOfMember(Long businessId, Long memberId) {
        return repository.findAllByBusinessIdAndMemberIdOrderByPurchaseDateTimeAsc(businessId, memberId);
    }

    @Transactional(readOnly = true)
    public CreditSale findSale(Long businessId, Long saleId) {
        return repository.findByIdAndBusinessId(saleId, businessId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró la venta " + saleId + "."));
    }

    /** Marca pagada la cuota (o el pago único) dentro de la transacción del pago. */
    @Transactional
    public void markPaid(Long businessId, Long saleId, Long installmentId) {
        var sale = findSale(businessId, saleId);
        sale.markPaid(installmentId);
        repository.save(sale);
    }
}
