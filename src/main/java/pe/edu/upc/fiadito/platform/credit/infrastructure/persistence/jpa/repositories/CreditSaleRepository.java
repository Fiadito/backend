package pe.edu.upc.fiadito.platform.credit.infrastructure.persistence.jpa.repositories;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.upc.fiadito.platform.credit.domain.model.aggregates.CreditSale;

import java.util.List;
import java.util.Optional;

public interface CreditSaleRepository extends JpaRepository<CreditSale, Long> {
    List<CreditSale> findAllByBusinessIdOrderByPurchaseDateTimeAsc(Long businessId);

    List<CreditSale> findAllByBusinessIdAndMemberIdOrderByPurchaseDateTimeAsc(Long businessId, Long memberId);

    List<CreditSale> findAllByMemberId(Long memberId);

    Optional<CreditSale> findByIdAndBusinessId(Long id, Long businessId);

    /** Bloquea la fila de la venta hasta el fin de la transacción (evita pagar dos veces la misma obligación). */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from CreditSale s where s.id = :id and s.businessId = :businessId")
    Optional<CreditSale> findForUpdate(@Param("id") Long id, @Param("businessId") Long businessId);
}
