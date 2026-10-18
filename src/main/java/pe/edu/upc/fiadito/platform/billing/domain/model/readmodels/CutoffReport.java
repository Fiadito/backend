package pe.edu.upc.fiadito.platform.billing.domain.model.readmodels;

import pe.edu.upc.fiadito.platform.credit.domain.model.valueobjects.PaymentCalendar;
import pe.edu.upc.fiadito.platform.membership.domain.model.aggregates.Member;
import pe.edu.upc.fiadito.platform.shared.domain.model.valueobjects.Rounding;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

/**
 * Modelo de lectura: reporte de pago de un corte (módulo 7.8, GenerarReporteCorte).
 * Por socio, ordenadas por fecha de compra, las compras a pago único y las cuotas que vencen en la
 * fecha de pago siguiente al corte. D = Σ S_j + Σ C_k.
 */
public record CutoffReport(LocalDate cutoffDate, LocalDate paymentDate, boolean preliminary, List<CutoffGroup> groups) {

    public static CutoffReport build(LocalDate cutoffDate, List<Obligation> obligations, List<Member> members, LocalDate today) {
        var paymentDate = PaymentCalendar.paymentDateOfCutoff(cutoffDate);
        var due = obligations.stream().filter(o -> o.dueDate().equals(paymentDate)).toList();
        var groups = members.stream().map(member -> {
                    var items = due.stream().filter(o -> o.memberId().equals(member.getId()))
                            .sorted(Comparator.comparing((Obligation o) -> o.sale().getPurchaseDateTime())).toList();
                    var total = Rounding.money(items.stream().map(Obligation::amount).reduce(BigDecimal.ZERO, BigDecimal::add));
                    return new CutoffGroup(member, items, total);
                })
                .filter(group -> !group.items().isEmpty())
                .toList();
        return new CutoffReport(cutoffDate, paymentDate, !today.isAfter(cutoffDate), groups);
    }

    public BigDecimal total() {
        return Rounding.money(groups.stream().map(CutoffGroup::total).reduce(BigDecimal.ZERO, BigDecimal::add));
    }

    public int itemCount() {
        return groups.stream().mapToInt(g -> g.items().size()).sum();
    }
}
