package pe.edu.upc.fiadito.platform.catalog.domain.model.valueobjects;

import java.math.BigDecimal;
import java.util.List;

/**
 * Tipo de ítem del catálogo: un producto suelto o un combo de productos a precio fijo (S/ 100, 200 o 400).
 * Los combos evitan vender, por ejemplo, 134 bebidas de S/ 3 para llegar al mínimo de S/ 400 por compra.
 */
public enum ProductType {
    PRODUCTO, COMBO;

    /** Precios permitidos para un combo. */
    public static final List<BigDecimal> COMBO_PRICES = List.of(new BigDecimal("100.00"), new BigDecimal("200.00"), new BigDecimal("400.00"));

    public static boolean isComboPrice(BigDecimal price) {
        return price != null && COMBO_PRICES.stream().anyMatch(p -> p.compareTo(price) == 0);
    }
}
