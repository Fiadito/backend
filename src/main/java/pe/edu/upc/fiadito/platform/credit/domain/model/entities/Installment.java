package pe.edu.upc.fiadito.platform.credit.domain.model.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pe.edu.upc.fiadito.platform.credit.domain.model.valueobjects.DebtStatus;
import pe.edu.upc.fiadito.platform.credit.domain.model.valueobjects.ScheduleRow;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Entidad CUOTA: una cuota quincenal del plan de pagos de una venta (interés + amortización).
 */
@Entity
@Table(name = "cuota", uniqueConstraints = @UniqueConstraint(name = "uk_cuota_venta_numero", columnNames = {"id_venta", "numero"}))
@Getter
@NoArgsConstructor
public class Installment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cuota")
    private Long id;

    @Column(name = "id_venta", nullable = false, insertable = false, updatable = false)
    private Long saleId;

    @Column(name = "numero", nullable = false)
    private Integer number;

    @Column(name = "fecha_vencimiento", nullable = false)
    private LocalDate dueDate;

    @Column(name = "saldo_inicial", nullable = false, precision = 10, scale = 2)
    private BigDecimal openingBalance;

    /** Interés compensatorio de la cuota: I = SI × TEQ. */
    @Column(name = "interes", nullable = false, precision = 10, scale = 2)
    private BigDecimal interest;

    @Column(name = "amortizacion", nullable = false, precision = 10, scale = 2)
    private BigDecimal amortization;

    @Column(name = "monto_cuota", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "saldo_final", nullable = false, precision = 10, scale = 2)
    private BigDecimal closingBalance;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "estado", nullable = false, length = 10)
    private DebtStatus status;

    public Installment(ScheduleRow row) {
        this.number = row.number();
        this.dueDate = row.dueDate();
        this.status = DebtStatus.PENDIENTE;
        reschedule(row);
    }

    /** Aplica los montos de una fila del plan (al crear la cuota o al recalcularla por cambio de tasa). */
    public void reschedule(ScheduleRow row) {
        this.openingBalance = row.openingBalance();
        this.interest = row.interest();
        this.amortization = row.amortization();
        this.amount = row.amount();
        this.closingBalance = row.closingBalance();
    }

    public boolean isPaid() {
        return status == DebtStatus.PAGADA;
    }

    public void markPaid() {
        this.status = DebtStatus.PAGADA;
    }

    /** PENDIENTE, PAGADA o EN_MORA (vencida y sin pagar) a una fecha. */
    public DebtStatus statusOn(LocalDate today) {
        if (isPaid()) return DebtStatus.PAGADA;
        return dueDate.isBefore(today) ? DebtStatus.EN_MORA : DebtStatus.PENDIENTE;
    }
}
