package pe.edu.upc.fiadito.platform.iam.domain.model.aggregates;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pe.edu.upc.fiadito.platform.iam.domain.model.valueobjects.Role;
import pe.edu.upc.fiadito.platform.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;

/**
 * Agregado USUARIO: cuenta de acceso del negocio o de un socio. La contraseña se guarda con BCrypt.
 */
@Entity
@Table(name = "usuario", uniqueConstraints = {
        @UniqueConstraint(name = "uk_usuario_correo", columnNames = "correo"),
        @UniqueConstraint(name = "uk_usuario_socio", columnNames = "id_socio")})
@AttributeOverride(name = "id", column = @Column(name = "id_usuario"))
@Getter
@NoArgsConstructor
public class User extends AuditableAbstractAggregateRoot<User> {

    @Column(name = "id_negocio", nullable = false)
    private Long businessId;

    /** Socio dueño de la cuenta (nulo si el rol es NEGOCIO). */
    @Column(name = "id_socio")
    private Long memberId;

    @Column(name = "nombre", nullable = false, length = 100)
    private String name;

    @Column(name = "correo", nullable = false, length = 100)
    private String email;

    @Column(name = "clave_hash", nullable = false, length = 255)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "rol", nullable = false, length = 10)
    private Role role;

    public User(Long businessId, Long memberId, String name, String email, String passwordHash, Role role) {
        this.businessId = businessId;
        this.memberId = memberId;
        this.name = name;
        this.email = email.trim().toLowerCase();
        this.passwordHash = passwordHash;
        this.role = role;
    }

    public void rename(String name) {
        this.name = name;
    }
}
