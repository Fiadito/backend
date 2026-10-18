package pe.edu.upc.fiadito.platform.billing.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upc.fiadito.platform.billing.domain.model.aggregates.Payment;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findAllByBusinessIdOrderByPaymentDateDescIdDesc(Long businessId);

    List<Payment> findAllByBusinessIdAndMemberIdOrderByPaymentDateDescIdDesc(Long businessId, Long memberId);

    boolean existsByInstallmentId(Long installmentId);

    boolean existsBySaleIdAndInstallmentIdIsNull(Long saleId);
}
