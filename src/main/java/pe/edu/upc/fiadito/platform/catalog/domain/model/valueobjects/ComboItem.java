package pe.edu.upc.fiadito.platform.catalog.domain.model.valueobjects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Value object: un producto y su cantidad dentro de un combo (tabla COMBO_ITEM).
 */
@Embeddable
@Getter
@NoArgsConstructor
public class ComboItem {

    @Column(name = "id_producto", nullable = false)
    private Long productId;

    @Column(name = "cantidad", nullable = false)
    private Integer quantity;

    public ComboItem(Long productId, Integer quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }
}
