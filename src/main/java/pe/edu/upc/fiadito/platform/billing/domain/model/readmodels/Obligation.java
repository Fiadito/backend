package pe.edu.upc.fiadito.platform.billing.domain.model.readmodels;

import pe.edu.upc.fiadito.platform.billing.domain.model.aggregates.Payment;
import pe.edu.upc.fiadito.platform.credit.domain.model.aggregates.CreditSale;
import pe.edu.upc.fiadito.platform.credit.domain.model.entities.Installment;
import pe.edu.upc.fiadito.platform.credit.domain.model.valueobjects.DebtStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Modelo de lectura: algo que el socio debe pagar en una fecha de pago. Es una cuota de una venta en cuotas
 * o la compra completa a pago único. Se arma con VENTA, CUOTA y PAGO; no es una tabla.
 */
public record Obligation(CreditSale sale, Installment installment, Payment payment) {

    public String key() {
        return installment != null ? "C-" + installment.getId() : "V-" + sale.getId();
    }

    public String code() {
        return installment != null ? String.format("#OB-%06d", installment.getId()) : String.format("#OB-%06dU", sale.getId());
    }

    public Long memberId() {
        return sale.getMemberId();
    }

    public LocalDate dueDate() {
        return installment != null ? installment.getDueDate() : sale.getPaymentDate();
    }

    /** D: monto exigible en la fecha de pago (interés compensatorio + capital). */
    public BigDecimal amount() {
        return installment != null ? installment.getAmount() : sale.getTotalToPay();
    }

    /** Interés compensatorio incluido en D. */
    public BigDecimal interest() {
        return installment != null ? installment.getInterest() : sale.getInterestTotal();
    }

    /** Capital (amortización) incluido en D. */
    public BigDecimal capital() {
        return installment != null ? installment.getAmortization() : sale.getAmount();
    }

    public String label() {
        return installment != null ? "Cuota " + installment.getNumber() + " de " + sale.getInstallmentCount() : "Pago único";
    }

    public boolean isPaid() {
        return payment != null || (installment != null ? installment.isPaid() : sale.isPaid());
    }

    public DebtStatus statusOn(LocalDate today) {
        if (isPaid()) return DebtStatus.PAGADA;
        return dueDate().isBefore(today) ? DebtStatus.EN_MORA : DebtStatus.PENDIENTE;
    }
}
