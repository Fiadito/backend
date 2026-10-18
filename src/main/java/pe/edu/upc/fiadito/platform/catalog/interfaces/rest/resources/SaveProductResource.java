package pe.edu.upc.fiadito.platform.catalog.interfaces.rest.resources;

import pe.edu.upc.fiadito.platform.catalog.domain.model.commands.SaveProductCommand;
import pe.edu.upc.fiadito.platform.catalog.domain.model.valueobjects.SaleMode;

import java.math.BigDecimal;

public record SaveProductResource(String descripcion, String marca, String proveedor, String unidadMedida, String imagenUrl,
                                  BigDecimal precioContado, BigDecimal precioLista, SaleMode modalidadVenta) {
    public SaveProductCommand toCommand(Long businessId, Long productId) {
        return new SaveProductCommand(businessId, productId, descripcion, marca, proveedor, unidadMedida, imagenUrl,
                precioContado, precioLista, modalidadVenta);
    }
}
