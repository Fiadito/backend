package pe.edu.upc.fiadito.platform.credit.domain.services;

import pe.edu.upc.fiadito.platform.credit.domain.model.aggregates.CreditSale;

import java.util.List;
import java.util.Optional;

public interface CreditSaleQueryService {
    List<CreditSale> getAllByBusiness(Long businessId);

    List<CreditSale> getAllByMember(Long businessId, Long memberId);

    Optional<CreditSale> getById(Long businessId, Long saleId);
}
