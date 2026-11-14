package pe.edu.upc.fiadito.platform.catalog.interfaces.rest.resources;

import pe.edu.upc.fiadito.platform.catalog.domain.model.aggregates.Product;
import pe.edu.upc.fiadito.platform.shared.domain.model.valueobjects.Rounding;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Producto o combo. Un combo trae su contenido y el valor de esos productos a precio de lista (valorContenido),
 * para comparar con su precio fijo.
 */
public record ProductResource(Long id, Long idNegocio, String tipo, String descripcion, String marca, String proveedor, String unidadMedida,
                              String imagenUrl, BigDecimal precioContado, BigDecimal precioLista, String modalidadVenta, String estado,
                              List<Item> items, BigDecimal valorContenido) {

    public record Item(Long idProducto, String producto, Integer cantidad, BigDecimal precioLista, BigDecimal subtotal) {
    }

    /** @param catalog productos del negocio por id (para el nombre y precio del contenido de un combo) */
    public static ProductResource from(Product p, Map<Long, Product> catalog) {
        var items = p.getItems().stream().map(i -> {
            var product = catalog.get(i.getProductId());
            var price = product == null ? BigDecimal.ZERO : product.getListPrice();
            return new Item(i.getProductId(), product == null ? "" : product.getBrand() + " " + product.getDescription(), i.getQuantity(),
                    price, Rounding.money(price.multiply(BigDecimal.valueOf(i.getQuantity()))));
        }).toList();
        var value = p.isCombo() ? Rounding.money(items.stream().map(Item::subtotal).reduce(BigDecimal.ZERO, BigDecimal::add)) : null;
        return new ProductResource(p.getId(), p.getBusinessId(), p.getType().name(), p.getDescription(), p.getBrand(), p.getSupplier(),
                p.getUnit(), p.getImageUrl(), p.getCashPrice(), p.getListPrice(), p.getSaleMode().name(), p.getStatus().name(), items, value);
    }
}
