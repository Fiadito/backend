package pe.edu.upc.fiadito.platform.membership.infrastructure.persistence.jpa.repositories;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.upc.fiadito.platform.membership.domain.model.aggregates.Member;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    List<Member> findAllByBusinessIdOrderByFullNameAsc(Long businessId);

    Optional<Member> findByIdAndBusinessId(Long id, Long businessId);

    boolean existsByBusinessIdAndDni(Long businessId, String dni);

    boolean existsByBusinessIdAndDniAndIdNot(Long businessId, String dni, Long id);

    /** Bloquea la fila del socio hasta el fin de la transacción (ventas simultáneas no superan su límite). */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from Member m where m.id = :id and m.businessId = :businessId")
    Optional<Member> findForUpdate(@Param("id") Long id, @Param("businessId") Long businessId);
}
