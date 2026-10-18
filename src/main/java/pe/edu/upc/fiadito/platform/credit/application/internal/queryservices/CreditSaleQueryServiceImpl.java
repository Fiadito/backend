package pe.edu.upc.fiadito.platform.credit.application.internal.queryservices;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.fiadito.platform.credit.domain.model.aggregates.CreditSale;
import pe.edu.upc.fiadito.platform.credit.domain.services.CreditSaleQueryService;
import pe.edu.upc.fiadito.platform.credit.infrastructure.persistence.jpa.repositories.CreditSaleRepository;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CreditSaleQueryServiceImpl implements CreditSaleQueryService {

    private final CreditSaleRepository repository;

    @Override
    public List<CreditSale> getAllByBusiness(Long businessId) {
        return repository.findAllByBusinessIdOrderByPurchaseDateTimeAsc(businessId);
    }

    @Override
    public List<CreditSale> getAllByMember(Long businessId, Long memberId) {
        return repository.findAllByBusinessIdAndMemberIdOrderByPurchaseDateTimeAsc(businessId, memberId);
    }

    @Override
    public Optional<CreditSale> getById(Long businessId, Long saleId) {
        return repository.findByIdAndBusinessId(saleId, businessId);
    }
}
