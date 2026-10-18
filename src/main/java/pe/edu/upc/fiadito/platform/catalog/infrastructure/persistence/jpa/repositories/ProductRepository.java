package pe.edu.upc.fiadito.platform.catalog.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upc.fiadito.platform.catalog.domain.model.aggregates.Product;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findAllByBusinessIdOrderByIdAsc(Long businessId);

    Optional<Product> findByIdAndBusinessId(Long id, Long businessId);
}
