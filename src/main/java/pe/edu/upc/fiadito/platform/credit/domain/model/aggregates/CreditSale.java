package pe.edu.upc.fiadito.platform.credit.domain.model.aggregates;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pe.edu.upc.fiadito.platform.credit.domain.model.entities.Installment;
import pe.edu.upc.fiadito.platform.credit.domain.model.valueobjects.DebtStatus;
import pe.edu.upc.fiadito.platform.credit.domain.model.valueobjects.InstallmentSchedule;
import pe.edu.upc.fiadito.platform.credit.domain.model.valueobjects.PaymentMode;
import pe.edu.upc.fiadito.platform.credit.domain.model.valueobjects.SaleSimulation;
import pe.edu.upc.fiadito.platform.shared.domain.exceptions.BusinessRuleException;
import pe.edu.upc.fiadito.platform.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import pe.edu.upc.fiadito.platform.shared.domain.model.valueobjects.EffectiveRate;
import pe.edu.upc.fiadito.platform.shared.domain.model.valueobjects.RatePeriod;
import pe.edu.upc.fiadito.platform.shared.domain.model.valueobjects.Rounding;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Agregado VENTA: compra al crédito de un socio con su tasa efectiva y su plan de pagos (CUOTA).
 * La tasa se pacta en cada venta (TEA equivalente entre 15 % y 27 %) y se puede cambiar después:
 * las cuotas pagadas no cambian y las pendientes se recalculan desde su saldo.
 */
@Entity
@Table(name = "venta")
@AttributeOverride(name = "id", column = @Column(name = "id_venta"))
@Getter
@NoArgsConstructor
public class CreditSale extends AuditableAbstractAggregateRoot<CreditSale> {

    @Column(name = "id_negocio", nullable = false)
    private Long businessId;

    @Column(name = "id_socio", nullable = false)
    private Long memberId;

    @Column(name = "id_producto", nullable = false)
    private Long productId;

    @Column(name = "cantidad", nullable = false)
    private Integer quantity;

    @Column(name = "precio_lista", nullable = false, precision = 10, scale = 2)
    private BigDecimal listPrice;

    /** P: precio de lista × cantidad (capital). */
    @Column(name = "monto", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "fecha_hora_compra", nullable = false)
    private LocalDateTime purchaseDateTime;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "modalidad", nullable = false, length = 10)
    private PaymentMode paymentMode;

    @Column(name = "n_cuotas", nullable = false)
    private Integer installmentCount;

    /** Tasa efectiva pactada en la venta: periodo y valor. */
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "periodo_tasa", nullable = false, length = 3)
    private RatePeriod ratePeriod;

    @Column(name = "valor_tasa", nullable = false, precision = 12, scale = 9)
    private BigDecimal rateValue;

    /** TEQ: tasa efectiva quincenal que se aplica en cada cuota. */
    @Column(name = "tasa_quincenal", nullable = false, precision = 12, scale = 9)
    private BigDecimal periodRate;

    /** Fecha del pago único o de la primera cuota. */
    @Column(name = "fecha_pago", nullable = false)
    private LocalDate paymentDate;

    /** Cuota fija (o el total del pago único). */
    @Column(name = "cuota", nullable = false, precision = 10, scale = 2)
    private BigDecimal installmentAmount;

    @Column(name = "interes_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal interestTotal;

    /** Capital + interés compensatorio. */
    @Column(name = "total_a_pagar", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalToPay;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "estado", nullable = false, length = 10)
    private DebtStatus status;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "id_venta", nullable = false)
    @OrderBy("number ASC")
    private List<Installment> installments = new ArrayList<>();

    /** Crea la venta a partir de una simulación válida (RegistrarVenta). */
    public CreditSale(Long businessId, Long memberId, Long productId, Integer quantity, LocalDateTime purchaseDateTime, SaleSimulation s) {
        if (!s.isValid()) throw new BusinessRuleException(s.errors().getFirst());
        this.businessId = businessId;
        this.memberId = memberId;
        this.productId = productId;
        this.quantity = quantity;
        this.listPrice = s.listPrice();
        this.amount = s.amount();
        this.purchaseDateTime = purchaseDateTime;
        this.paymentMode = s.paymentMode();
        this.installmentCount = s.installmentCount();
        this.ratePeriod = s.ratePeriod();
        this.rateValue = s.rateValue();
        this.periodRate = s.periodRate();
        this.paymentDate = s.paymentDate();
        this.installmentAmount = s.installmentAmount();
        this.interestTotal = s.interestTotal();
        this.totalToPay = s.totalToPay();
        this.status = DebtStatus.PENDIENTE;
        s.schedule().forEach(row -> installments.add(new Installment(row)));
    }

    public boolean isPaid() {
        return status == DebtStatus.PAGADA;
    }

    public boolean isInInstallments() {
        return paymentMode == PaymentMode.CUOTAS;
    }

    public EffectiveRate rate() {
        return new EffectiveRate(ratePeriod, rateValue);
    }

    /** Estado a una fecha: PAGADA, EN_MORA (alguna obligación vencida) o PENDIENTE. */
    public DebtStatus statusOn(LocalDate today) {
        if (isPaid()) return DebtStatus.PAGADA;
        if (isInInstallments()) {
            return installments.stream().anyMatch(i -> i.statusOn(today) == DebtStatus.EN_MORA) ? DebtStatus.EN_MORA : DebtStatus.PENDIENTE;
        }
        return paymentDate.isBefore(today) ? DebtStatus.EN_MORA : DebtStatus.PENDIENTE;
    }

    /** Capital pendiente (lo que cuenta para el límite de crédito): amortizaciones sin pagar o el monto del pago único. */
    public BigDecimal pendingCapital() {
        if (isPaid()) return BigDecimal.ZERO;
        if (!isInInstallments()) return amount;
        return installments.stream().filter(i -> !i.isPaid()).map(Installment::getAmortization).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Installment installment(Long installmentId) {
        return installments.stream().filter(i -> i.getId().equals(installmentId)).findFirst()
                .orElseThrow(() -> new BusinessRuleException("La cuota no pertenece a esta venta."));
    }

    /**
     * CambiarTasa: aplica una nueva tasa efectiva. Las cuotas pagadas se mantienen; las pendientes se recalculan
     * con el método francés sobre su saldo de capital, en sus mismas fechas.
     */
    public void changeRate(EffectiveRate rate) {
        if (isPaid()) throw new BusinessRuleException("La venta ya está pagada; su tasa no se puede cambiar.");
        this.ratePeriod = rate.period();
        this.rateValue = rate.value();
        this.periodRate = rate.toPeriod(RatePeriod.TEQ);
        if (!isInInstallments()) {
            this.interestTotal = InstallmentSchedule.singlePaymentInterest(amount, periodRate);
            this.totalToPay = Rounding.money(amount.add(interestTotal));
            this.installmentAmount = totalToPay;
            return;
        }
        var pending = installments.stream().filter(i -> !i.isPaid()).sorted(Comparator.comparing(Installment::getNumber)).toList();
        var balance = pending.stream().map(Installment::getAmortization).reduce(BigDecimal.ZERO, BigDecimal::add);
        var rows = InstallmentSchedule.build(balance, periodRate, pending.stream().map(Installment::getDueDate).toList());
        for (int k = 0; k < pending.size(); k++) pending.get(k).reschedule(rows.get(k));
        this.installmentAmount = rows.getFirst().amount();
        this.interestTotal = installments.stream().map(Installment::getInterest).reduce(BigDecimal.ZERO, BigDecimal::add);
        this.totalToPay = installments.stream().map(Installment::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** MarcarObligacionPagada: la cuota (o el pago único) y la venta cuando todo está pagado. */
    public void markPaid(Long installmentId) {
        if (isInInstallments()) {
            if (installmentId == null) throw new BusinessRuleException("Indica la cuota que se paga.");
            var installment = installment(installmentId);
            if (installment.isPaid()) throw new BusinessRuleException("Esta cuota ya fue pagada.");
            installment.markPaid();
            if (installments.stream().allMatch(Installment::isPaid)) status = DebtStatus.PAGADA;
        } else {
            if (isPaid()) throw new BusinessRuleException("Esta compra ya fue pagada.");
            status = DebtStatus.PAGADA;
        }
    }
}
