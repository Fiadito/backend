package pe.edu.upc.fiadito.platform.catalog.domain.model.commands;

import pe.edu.upc.fiadito.platform.catalog.domain.model.valueobjects.SaleMode;

import java.math.BigDecimal;

/** Registro (productId nulo) o edición de un producto. */
public record SaveProductCommand(Long businessId, Long productId, String description, String brand, String supplier, String unit,
                                 String imageUrl, BigDecimal cashPrice, BigDecimal listPrice, SaleMode saleMode) {
}
