package pe.edu.upc.fiadito.platform.membership.domain.model.aggregates;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pe.edu.upc.fiadito.platform.credit.domain.model.valueobjects.CreditPolicy;
import pe.edu.upc.fiadito.platform.membership.domain.model.valueobjects.Currency;
import pe.edu.upc.fiadito.platform.membership.domain.model.valueobjects.RecordStatus;
import pe.edu.upc.fiadito.platform.shared.domain.exceptions.BusinessRuleException;
import pe.edu.upc.fiadito.platform.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;

import java.math.BigDecimal;

/**
 * Agregado SOCIO: datos del socio y sus condiciones de crédito (módulo 7.4, GuardarSocio).
 * Las tasas ya no son del socio: la tasa efectiva se pacta en cada venta y la moratoria se coloca al pagar algo vencido.
 */
@Entity
@Table(name = "socio", uniqueConstraints = @UniqueConstraint(name = "uk_socio_negocio_dni", columnNames = {"id_negocio", "dni"}))
@AttributeOverride(name = "id", column = @Column(name = "id_socio"))
@Getter
@NoArgsConstructor
public class Member extends AuditableAbstractAggregateRoot<Member> {

    @Column(name = "id_negocio", nullable = false)
    private Long businessId;

    @Column(name = "dni", nullable = false, length = 8, columnDefinition = "CHAR(8)")
    private String dni;

    @Column(name = "nombres", nullable = false, length = 100)
    private String fullName;

    @Column(name = "celular", length = 9)
    private String phone;

    @Column(name = "direccion", length = 150)
    private String address;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "moneda", nullable = false, length = 3)
    private Currency currency;

    @Column(name = "limite_credito", nullable = false, precision = 10, scale = 2)
    private BigDecimal creditLimit;

    @Column(name = "max_cuotas", nullable = false)
    private Integer maxInstallments;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "estado", nullable = false, length = 10)
    private RecordStatus status;

    public Member(Long businessId, String dni, String fullName, String phone, String address, Currency currency,
                  BigDecimal creditLimit, Integer maxInstallments) {
        this.businessId = businessId;
        this.status = RecordStatus.ACTIVO;
        update(dni, fullName, phone, address, currency, creditLimit, maxInstallments, BigDecimal.ZERO);
    }

    /**
     * Aplica y valida los datos del socio.
     *
     * @param currentDebt capital que el socio debe hoy: el límite no puede quedar por debajo
     */
    public void update(String dni, String fullName, String phone, String address, Currency currency,
                       BigDecimal creditLimit, Integer maxInstallments, BigDecimal currentDebt) {
        var cleanPhone = phone == null ? "" : phone.replaceAll("\\s", "");
        if (dni == null || !dni.matches("\\d{8}")) throw new BusinessRuleException("El DNI debe tener 8 dígitos.");
        if (fullName == null || fullName.trim().length() < 3) throw new BusinessRuleException("Ingresa los nombres y apellidos del socio.");
        if (!cleanPhone.isEmpty() && !cleanPhone.matches("9\\d{8}")) throw new BusinessRuleException("El celular debe tener 9 dígitos y empezar con 9.");
        if (creditLimit == null || !CreditPolicy.isAmountInRange(creditLimit))
            throw new BusinessRuleException("El límite debe estar entre S/ 400.00 y S/ 1,000.00.");
        if (currentDebt != null && creditLimit.compareTo(currentDebt) < 0)
            throw new BusinessRuleException("El límite no puede ser menor que la deuda actual del socio (S/ " + currentDebt.setScale(2) + ").");
        if (maxInstallments == null || maxInstallments < 1 || maxInstallments > CreditPolicy.MAX_INSTALLMENTS)
            throw new BusinessRuleException("Las cuotas máximas deben estar entre 1 y 8.");
        this.dni = dni;
        this.fullName = fullName.trim();
        this.phone = cleanPhone.isEmpty() ? null : cleanPhone;
        this.address = address == null ? null : address.trim();
        this.currency = currency == null ? Currency.PEN : currency;
        this.creditLimit = creditLimit;
        this.maxInstallments = maxInstallments;
    }

    public boolean isActive() {
        return status == RecordStatus.ACTIVO;
    }

    /** DarDeBajaSocio: solo si no tiene deuda pendiente. */
    public void deactivate(BigDecimal pendingDebt) {
        if (pendingDebt.signum() > 0) throw new BusinessRuleException("No se puede dar de baja a un socio con deuda.");
        this.status = RecordStatus.INACTIVO;
    }

    public void activate() {
        this.status = RecordStatus.ACTIVO;
    }
}
