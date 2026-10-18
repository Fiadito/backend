package pe.edu.upc.fiadito.platform.catalog.application.internal.queryservices;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.fiadito.platform.catalog.domain.model.aggregates.Product;
import pe.edu.upc.fiadito.platform.catalog.domain.services.ProductQueryService;
import pe.edu.upc.fiadito.platform.catalog.infrastructure.persistence.jpa.repositories.ProductRepository;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ProductQueryServiceImpl implements ProductQueryService {

    private final ProductRepository repository;

    @Override
    public List<Product> getAllByBusiness(Long businessId) {
        return repository.findAllByBusinessIdOrderByIdAsc(businessId);
    }

    @Override
    public Optional<Product> getById(Long businessId, Long productId) {
        return repository.findByIdAndBusinessId(productId, businessId);
    }
}
