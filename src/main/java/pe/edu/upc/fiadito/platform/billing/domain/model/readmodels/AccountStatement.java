package pe.edu.upc.fiadito.platform.billing.domain.model.readmodels;

import pe.edu.upc.fiadito.platform.billing.domain.model.aggregates.Payment;
import pe.edu.upc.fiadito.platform.billing.domain.model.valueobjects.LateInterest;
import pe.edu.upc.fiadito.platform.credit.domain.model.aggregates.CreditSale;
import pe.edu.upc.fiadito.platform.credit.domain.model.valueobjects.DebtStatus;
import pe.edu.upc.fiadito.platform.membership.domain.model.aggregates.Member;
import pe.edu.upc.fiadito.platform.shared.domain.model.valueobjects.Rounding;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Modelo de lectura: estado de cuenta del socio (módulo 7.10, MostrarEstadoCuenta).
 * Deuda capital = obligaciones pendientes; deuda a hoy = deuda capital + mora acumulada a hoy;
 * crédito disponible = límite − deuda capital.
 */
public class AccountStatement {

    private final Member member;
    private final LocalDate today;
    private final List<CreditSale> sales;
    private final List<Obligation> obligations;
    private final List<PendingObligation> pending;
    private final List<Payment> payments;

    public AccountStatement(Member member, List<CreditSale> sales, List<Obligation> obligations, List<Payment> payments, LocalDate today) {
        this.member = member;
        this.today = today;
        this.sales = sales;
        this.obligations = obligations;
        this.payments = payments;
        this.pending = obligations.stream().filter(o -> !o.isPaid())
                .map(o -> new PendingObligation(o, LateInterest.quote(o.amount(), o.dueDate(), today, member.lateRate()), o.statusOn(today)))
                .toList();
    }

    public Member member() { return member; }
    public LocalDate today() { return today; }
    public List<CreditSale> sales() { return sales; }
    public List<Obligation> obligations() { return obligations; }
    public List<PendingObligation> pending() { return pending; }
    public List<Payment> payments() { return payments; }

    public BigDecimal capitalDebt() {
        return Rounding.money(pending.stream().map(p -> p.obligation().amount()).reduce(BigDecimal.ZERO, BigDecimal::add));
    }

    public BigDecimal lateInterestToday() {
        return Rounding.money(pending.stream().map(p -> p.quoteToday().lateInterest()).reduce(BigDecimal.ZERO, BigDecimal::add));
    }

    public BigDecimal debtToday() {
        return Rounding.money(pending.stream().map(p -> p.quoteToday().total()).reduce(BigDecimal.ZERO, BigDecimal::add));
    }

    public BigDecimal availableCredit() {
        return Rounding.money(member.getCreditLimit().subtract(capitalDebt()));
    }

    public boolean isOverdue() {
        return pending.stream().anyMatch(p -> p.status() == DebtStatus.EN_MORA);
    }

    /** Próximo pago: la fecha pendiente más cercana y todo lo que vence ese día (con mora a hoy). */
    public NextPayment nextPayment() {
        if (pending.isEmpty()) return null;
        var date = pending.getFirst().obligation().dueDate();
        var due = pending.stream().filter(p -> p.obligation().dueDate().equals(date)).toList();
        return new NextPayment(date, Rounding.money(due.stream().map(p -> p.quoteToday().total()).reduce(BigDecimal.ZERO, BigDecimal::add)), due.size());
    }
}
