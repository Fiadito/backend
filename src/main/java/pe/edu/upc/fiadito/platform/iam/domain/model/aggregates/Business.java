package pe.edu.upc.fiadito.platform.iam.domain.model.aggregates;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pe.edu.upc.fiadito.platform.shared.domain.exceptions.BusinessRuleException;
import pe.edu.upc.fiadito.platform.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;

import java.time.LocalDateTime;

/**
 * Agregado NEGOCIO: el negocio que otorga los créditos.
 */
@Entity
@Table(name = "negocio", uniqueConstraints = @UniqueConstraint(name = "uk_negocio_ruc", columnNames = "ruc"))
@AttributeOverride(name = "id", column = @Column(name = "id_negocio"))
@Getter
@NoArgsConstructor
public class Business extends AuditableAbstractAggregateRoot<Business> {

    @Column(name = "ruc", nullable = false, length = 11, columnDefinition = "CHAR(11)")
    private String ruc;

    @Column(name = "nombre_comercial", nullable = false, length = 100)
    private String name;

    @Column(name = "giro", nullable = false, length = 50)
    private String line;

    @Column(name = "direccion", nullable = false, length = 150)
    private String address;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime registeredAt;

    public Business(String ruc, String name, String line, String address, LocalDateTime registeredAt) {
        if (ruc == null || !ruc.matches("\\d{11}")) throw new BusinessRuleException("El RUC debe tener 11 dígitos.");
        if (name == null || name.isBlank()) throw new BusinessRuleException("Ingresa el nombre comercial.");
        if (line == null || line.isBlank()) throw new BusinessRuleException("Selecciona el giro del negocio.");
        if (address == null || address.trim().length() < 5) throw new BusinessRuleException("Ingresa la dirección del negocio.");
        this.ruc = ruc;
        this.name = name.trim();
        this.line = line.trim();
        this.address = address.trim();
        this.registeredAt = registeredAt;
    }
}
