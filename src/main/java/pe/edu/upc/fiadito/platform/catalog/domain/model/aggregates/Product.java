package pe.edu.upc.fiadito.platform.catalog.domain.model.aggregates;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pe.edu.upc.fiadito.platform.catalog.domain.model.valueobjects.ComboItem;
import pe.edu.upc.fiadito.platform.catalog.domain.model.valueobjects.ProductType;
import pe.edu.upc.fiadito.platform.catalog.domain.model.valueobjects.SaleMode;
import pe.edu.upc.fiadito.platform.membership.domain.model.valueobjects.RecordStatus;
import pe.edu.upc.fiadito.platform.shared.domain.exceptions.BusinessRuleException;
import pe.edu.upc.fiadito.platform.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Agregado PRODUCTO: producto o servicio del negocio (módulo 7.5, GuardarProducto), o un COMBO de productos
 * con precio fijo de S/ 100, 200 o 400 (se vende igual que un producto: precio × cantidad).
 */
@Entity
@Table(name = "producto")
@AttributeOverride(name = "id", column = @Column(name = "id_producto"))
@Getter
@NoArgsConstructor
public class Product extends AuditableAbstractAggregateRoot<Product> {

    /** Tamaño máximo de la imagen (2 MB) expresado en caracteres de un data URL en base64. */
    private static final int MAX_IMAGE_CHARS = 2 * 1024 * 1024 * 4 / 3 + 100;

    @Column(name = "id_negocio", nullable = false)
    private Long businessId;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "tipo", nullable = false, length = 10)
    private ProductType type;

    @Column(name = "descripcion", nullable = false, length = 100)
    private String description;

    @Column(name = "marca", nullable = false, length = 50)
    private String brand;

    @Column(name = "proveedor", length = 100)
    private String supplier;

    @Column(name = "unidad_medida", nullable = false, length = 20)
    private String unit;

    /** Ruta de la imagen o data URL (JPG/PNG de máximo 2 MB). */
    @Lob
    @Column(name = "imagen_url", columnDefinition = "LONGTEXT")
    private String imageUrl;

    @Column(name = "precio_contado", nullable = false, precision = 10, scale = 2)
    private BigDecimal cashPrice;

    @Column(name = "precio_lista", nullable = false, precision = 10, scale = 2)
    private BigDecimal listPrice;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "modalidad_venta", nullable = false, length = 10)
    private SaleMode saleMode;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "estado", nullable = false, length = 10)
    private RecordStatus status;

    /** Contenido del combo (vacío si es un producto suelto). */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "combo_item", joinColumns = @JoinColumn(name = "id_combo"))
    private List<ComboItem> items = new ArrayList<>();

    public Product(Long businessId, ProductType type, String description, String brand, String supplier, String unit, String imageUrl,
                   BigDecimal cashPrice, BigDecimal listPrice, SaleMode saleMode, List<ComboItem> items) {
        this.businessId = businessId;
        this.status = RecordStatus.ACTIVO;
        update(type, description, brand, supplier, unit, imageUrl, cashPrice, listPrice, saleMode, items);
    }

    public void update(ProductType type, String description, String brand, String supplier, String unit, String imageUrl,
                       BigDecimal cashPrice, BigDecimal listPrice, SaleMode saleMode, List<ComboItem> items) {
        var kind = type == null ? ProductType.PRODUCTO : type;
        var contents = items == null ? List.<ComboItem>of() : items;
        if (description == null || description.trim().length() < 2) throw new BusinessRuleException("Ingresa la descripción del producto.");
        if (brand == null || brand.isBlank()) throw new BusinessRuleException("Ingresa la marca.");
        if (imageUrl != null && imageUrl.length() > MAX_IMAGE_CHARS) throw new BusinessRuleException("La imagen no debe pesar más de 2 MB.");
        if (kind == ProductType.COMBO) {
            if (!ProductType.isComboPrice(listPrice)) throw new BusinessRuleException("El precio de un combo debe ser S/ 100, S/ 200 o S/ 400.");
            if (contents.isEmpty()) throw new BusinessRuleException("Agrega al menos un producto al combo.");
            if (contents.stream().anyMatch(i -> i.getProductId() == null || i.getQuantity() == null || i.getQuantity() < 1))
                throw new BusinessRuleException("Cada producto del combo necesita una cantidad de 1 o más.");
            if (new HashSet<>(contents.stream().map(ComboItem::getProductId).toList()).size() != contents.size())
                throw new BusinessRuleException("Un producto no puede repetirse en el combo; sube su cantidad.");
            cashPrice = listPrice;
        } else {
            if (!contents.isEmpty()) throw new BusinessRuleException("Solo los combos llevan productos dentro.");
            if (cashPrice == null || cashPrice.signum() <= 0) throw new BusinessRuleException("El precio contado debe ser mayor que cero.");
            if (listPrice == null || listPrice.compareTo(cashPrice) < 0) throw new BusinessRuleException("El precio de lista debe ser mayor o igual al precio contado.");
        }
        this.type = kind;
        this.description = description.trim();
        this.brand = brand.trim();
        this.supplier = supplier == null ? null : supplier.trim();
        this.unit = unit == null || unit.isBlank() ? (kind == ProductType.COMBO ? "Combo" : "Unidad") : unit.trim();
        this.imageUrl = imageUrl;
        this.cashPrice = cashPrice;
        this.listPrice = listPrice;
        this.saleMode = saleMode == null ? SaleMode.AMBAS : saleMode;
        this.items.clear();
        this.items.addAll(contents);
    }

    public boolean isActive() {
        return status == RecordStatus.ACTIVO;
    }

    public boolean isCombo() {
        return type == ProductType.COMBO;
    }

    public void changeStatus(RecordStatus status) {
        this.status = status;
    }
}
