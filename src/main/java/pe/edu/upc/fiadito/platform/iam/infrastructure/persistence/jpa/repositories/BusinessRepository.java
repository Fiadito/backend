package pe.edu.upc.fiadito.platform.iam.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upc.fiadito.platform.iam.domain.model.aggregates.Business;

public interface BusinessRepository extends JpaRepository<Business, Long> {
    boolean existsByRuc(String ruc);
}
