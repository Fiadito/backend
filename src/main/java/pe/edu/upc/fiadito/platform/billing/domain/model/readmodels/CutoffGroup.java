package pe.edu.upc.fiadito.platform.billing.domain.model.readmodels;

import pe.edu.upc.fiadito.platform.membership.domain.model.aggregates.Member;

import java.math.BigDecimal;
import java.util.List;

/** Bloque de un socio en el reporte de corte. */
public record CutoffGroup(Member member, List<Obligation> items, BigDecimal total) {
}
