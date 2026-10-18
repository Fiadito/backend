package pe.edu.upc.fiadito.platform.catalog.interfaces.acl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.fiadito.platform.catalog.domain.model.aggregates.Product;
import pe.edu.upc.fiadito.platform.catalog.infrastructure.persistence.jpa.repositories.ProductRepository;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Fachada del contexto Catalog para Credit y Billing.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CatalogContextFacade {

    private final ProductRepository repository;

    public Optional<Product> findProduct(Long businessId, Long productId) {
        return repository.findByIdAndBusinessId(productId, businessId);
    }

    /** Nombre corto "Marca Descripción" de cada producto del negocio. */
    public Map<Long, String> productNames(Long businessId) {
        return repository.findAllByBusinessIdOrderByIdAsc(businessId).stream()
                .collect(Collectors.toMap(Product::getId, p -> p.getBrand() + " " + p.getDescription(), (a, b) -> a));
    }

    public Map<Long, Product> productsById(Long businessId) {
        return repository.findAllByBusinessIdOrderByIdAsc(businessId).stream().collect(Collectors.toMap(Product::getId, Function.identity()));
    }
}
