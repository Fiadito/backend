package pe.edu.upc.fiadito.platform.billing.domain.model.aggregates;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pe.edu.upc.fiadito.platform.billing.domain.model.valueobjects.PaymentMethod;
import pe.edu.upc.fiadito.platform.billing.domain.model.valueobjects.PaymentQuote;
import pe.edu.upc.fiadito.platform.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Agregado PAGO: pago completo de una cuota o de una compra a pago único, con su interés moratorio
 * y la imputación mora → capital.
 */
@Entity
@Table(name = "pago", uniqueConstraints = @UniqueConstraint(name = "uk_pago_cuota", columnNames = "id_cuota"))
@AttributeOverride(name = "id", column = @Column(name = "id_pago"))
@Getter
@NoArgsConstructor
public class Payment extends AuditableAbstractAggregateRoot<Payment> {

    @Column(name = "id_negocio", nullable = false)
    private Long businessId;

    @Column(name = "id_venta", nullable = false)
    private Long saleId;

    /** Cuota pagada (nula si la venta es a pago único). */
    @Column(name = "id_cuota")
    private Long installmentId;

    @Column(name = "id_socio", nullable = false)
    private Long memberId;

    @Column(name = "fecha_pago_real", nullable = false)
    private LocalDate paymentDate;

    @Column(name = "dias_mora", nullable = false)
    private Integer lateDays;

    @Column(name = "ted_mora", nullable = false, precision = 10, scale = 7)
    private BigDecimal lateDailyRate;

    @Column(name = "interes_mora", nullable = false, precision = 10, scale = 2)
    private BigDecimal lateInterest;

    @Column(name = "total_pagado", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPaid;

    @Column(name = "imputacion_mora", nullable = false, precision = 10, scale = 2)
    private BigDecimal lateImputation;

    @Column(name = "imputacion_capital", nullable = false, precision = 10, scale = 2)
    private BigDecimal capitalImputation;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "medio_pago", nullable = false, length = 20)
    private PaymentMethod method;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime registeredAt;

    public Payment(Long businessId, Long saleId, Long installmentId, Long memberId, LocalDate paymentDate, PaymentQuote quote,
                   PaymentMethod method, LocalDateTime registeredAt) {
        this.businessId = businessId;
        this.saleId = saleId;
        this.installmentId = installmentId;
        this.memberId = memberId;
        this.paymentDate = paymentDate;
        this.lateDays = quote.lateDays();
        this.lateDailyRate = quote.dailyRate();
        this.lateInterest = quote.lateInterest();
        this.totalPaid = quote.total();
        this.lateImputation = quote.lateImputation();
        this.capitalImputation = quote.capitalImputation();
        this.method = method;
        this.registeredAt = registeredAt;
    }
}
