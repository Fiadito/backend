package pe.edu.upc.fiadito.platform.catalog.interfaces.rest.resources;

import pe.edu.upc.fiadito.platform.catalog.domain.model.aggregates.Product;

import java.math.BigDecimal;

public record ProductResource(Long id, Long idNegocio, String descripcion, String marca, String proveedor, String unidadMedida,
                              String imagenUrl, BigDecimal precioContado, BigDecimal precioLista, String modalidadVenta, String estado) {
    public static ProductResource from(Product p) {
        return new ProductResource(p.getId(), p.getBusinessId(), p.getDescription(), p.getBrand(), p.getSupplier(), p.getUnit(),
                p.getImageUrl(), p.getCashPrice(), p.getListPrice(), p.getSaleMode().name(), p.getStatus().name());
    }
}
