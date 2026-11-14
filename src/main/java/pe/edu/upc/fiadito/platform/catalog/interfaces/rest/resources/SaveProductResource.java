package pe.edu.upc.fiadito.platform.catalog.interfaces.rest.resources;

import pe.edu.upc.fiadito.platform.catalog.domain.model.commands.SaveProductCommand;
import pe.edu.upc.fiadito.platform.catalog.domain.model.valueobjects.ComboItem;
import pe.edu.upc.fiadito.platform.catalog.domain.model.valueobjects.ProductType;
import pe.edu.upc.fiadito.platform.catalog.domain.model.valueobjects.SaleMode;

import java.math.BigDecimal;
import java.util.List;

/**
 * Cuerpo de POST/PUT /productos. tipo: PRODUCTO (por defecto) o COMBO; un combo lleva items [{idProducto, cantidad}]
 * y precioLista de 100, 200 o 400.
 */
public record SaveProductResource(ProductType tipo, String descripcion, String marca, String proveedor, String unidadMedida, String imagenUrl,
                                  BigDecimal precioContado, BigDecimal precioLista, SaleMode modalidadVenta, List<Item> items) {

    public record Item(Long idProducto, Integer cantidad) {
    }

    public SaveProductCommand toCommand(Long businessId, Long productId) {
        var contents = items == null ? List.<ComboItem>of() : items.stream().map(i -> new ComboItem(i.idProducto(), i.cantidad())).toList();
        return new SaveProductCommand(businessId, productId, tipo, descripcion, marca, proveedor, unidadMedida, imagenUrl,
                precioContado, precioLista, modalidadVenta, contents);
    }
}
