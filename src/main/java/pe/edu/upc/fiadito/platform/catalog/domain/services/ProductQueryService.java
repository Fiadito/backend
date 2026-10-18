package pe.edu.upc.fiadito.platform.catalog.domain.services;

import pe.edu.upc.fiadito.platform.catalog.domain.model.aggregates.Product;

import java.util.List;
import java.util.Optional;

public interface ProductQueryService {
    List<Product> getAllByBusiness(Long businessId);

    Optional<Product> getById(Long businessId, Long productId);
}
