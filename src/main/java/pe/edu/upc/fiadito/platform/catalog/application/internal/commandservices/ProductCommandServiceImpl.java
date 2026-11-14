package pe.edu.upc.fiadito.platform.catalog.application.internal.commandservices;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.fiadito.platform.catalog.domain.model.aggregates.Product;
import pe.edu.upc.fiadito.platform.catalog.domain.model.commands.ChangeProductStatusCommand;
import pe.edu.upc.fiadito.platform.catalog.domain.model.commands.SaveProductCommand;
import pe.edu.upc.fiadito.platform.catalog.domain.model.valueobjects.ProductType;
import pe.edu.upc.fiadito.platform.catalog.domain.services.ProductCommandService;
import pe.edu.upc.fiadito.platform.shared.domain.exceptions.BusinessRuleException;
import pe.edu.upc.fiadito.platform.catalog.infrastructure.persistence.jpa.repositories.ProductRepository;
import pe.edu.upc.fiadito.platform.membership.domain.model.valueobjects.RecordStatus;
import pe.edu.upc.fiadito.platform.shared.domain.exceptions.ResourceNotFoundException;
import pe.edu.upc.fiadito.platform.shared.domain.model.events.OperationPerformedEvent;

@Service
@Transactional
@RequiredArgsConstructor
public class ProductCommandServiceImpl implements ProductCommandService {

    private final ProductRepository repository;
    private final ApplicationEventPublisher events;

    @Override
    public Product handle(SaveProductCommand c, Long actorUserId, String actorName) {
        if (c.type() == ProductType.COMBO) validateComboContents(c);
        Product product;
        String action;
        if (c.productId() == null) {
            product = repository.save(new Product(c.businessId(), c.type(), c.description(), c.brand(), c.supplier(), c.unit(), c.imageUrl(),
                    c.cashPrice(), c.listPrice(), c.saleMode(), c.items()));
            action = "REGISTRAR";
        } else {
            product = find(c.businessId(), c.productId());
            product.update(c.type(), c.description(), c.brand(), c.supplier(), c.unit(), c.imageUrl(), c.cashPrice(), c.listPrice(),
                    c.saleMode(), c.items());
            product = repository.save(product);
            action = "EDITAR";
        }
        events.publishEvent(new OperationPerformedEvent(c.businessId(), actorUserId, actorName, action, "PRODUCTO", product.getId(),
                (c.productId() == null ? "Registro" : "Edición") + (product.isCombo() ? " del combo " : " del producto ") + product.getDescription()));
        return product;
    }

    /** El contenido de un combo son productos sueltos del mismo negocio (no otros combos ni el propio combo). */
    private void validateComboContents(SaveProductCommand c) {
        if (c.items() == null) return;
        for (var item : c.items()) {
            if (item.getProductId() == null) continue;
            var product = repository.findByIdAndBusinessId(item.getProductId(), c.businessId())
                    .orElseThrow(() -> new BusinessRuleException("El producto " + item.getProductId() + " del combo no existe."));
            if (product.isCombo()) throw new BusinessRuleException("Un combo no puede contener otro combo.");
        }
    }

    @Override
    public Product handle(ChangeProductStatusCommand c, Long actorUserId, String actorName) {
        var product = find(c.businessId(), c.productId());
        product.changeStatus(c.status());
        events.publishEvent(new OperationPerformedEvent(c.businessId(), actorUserId, actorName, "EDITAR", "PRODUCTO", product.getId(),
                (c.status() == RecordStatus.INACTIVO ? "Baja" : "Reactivación") + " del producto " + product.getDescription()));
        return repository.save(product);
    }

    private Product find(Long businessId, Long productId) {
        return repository.findByIdAndBusinessId(productId, businessId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el producto " + productId + "."));
    }
}
