package pe.edu.upc.fiadito.platform.membership.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upc.fiadito.platform.membership.domain.model.aggregates.Member;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    List<Member> findAllByBusinessIdOrderByFullNameAsc(Long businessId);

    Optional<Member> findByIdAndBusinessId(Long id, Long businessId);

    boolean existsByBusinessIdAndDni(Long businessId, String dni);

    boolean existsByBusinessIdAndDniAndIdNot(Long businessId, String dni, Long id);
}
