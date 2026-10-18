package pe.edu.upc.fiadito.platform.credit.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upc.fiadito.platform.credit.domain.model.aggregates.CreditSale;

import java.util.List;
import java.util.Optional;

public interface CreditSaleRepository extends JpaRepository<CreditSale, Long> {
    List<CreditSale> findAllByBusinessIdOrderByPurchaseDateTimeAsc(Long businessId);

    List<CreditSale> findAllByBusinessIdAndMemberIdOrderByPurchaseDateTimeAsc(Long businessId, Long memberId);

    List<CreditSale> findAllByMemberId(Long memberId);

    Optional<CreditSale> findByIdAndBusinessId(Long id, Long businessId);
}
