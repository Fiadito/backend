package pe.edu.upc.fiadito.platform.iam.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upc.fiadito.platform.iam.domain.model.aggregates.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    Optional<User> findByMemberId(Long memberId);

    boolean existsByEmail(String email);
}
