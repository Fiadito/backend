package pe.edu.upc.fiadito.platform.catalog.domain.model.commands;

import pe.edu.upc.fiadito.platform.catalog.domain.model.valueobjects.ComboItem;
import pe.edu.upc.fiadito.platform.catalog.domain.model.valueobjects.ProductType;
import pe.edu.upc.fiadito.platform.catalog.domain.model.valueobjects.SaleMode;

import java.math.BigDecimal;
import java.util.List;

/** Registro (productId nulo) o edición de un producto o de un combo (con su contenido). */
public record SaveProductCommand(Long businessId, Long productId, ProductType type, String description, String brand, String supplier,
                                 String unit, String imageUrl, BigDecimal cashPrice, BigDecimal listPrice, SaleMode saleMode,
                                 List<ComboItem> items) {
}
