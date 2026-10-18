package pe.edu.upc.fiadito.platform.billing.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pe.edu.upc.fiadito.platform.billing.domain.model.readmodels.Obligation;
import pe.edu.upc.fiadito.platform.billing.domain.services.BillingQueryService;
import pe.edu.upc.fiadito.platform.billing.interfaces.rest.resources.*;
import pe.edu.upc.fiadito.platform.billing.interfaces.rest.transform.BillingResourceAssembler;
import pe.edu.upc.fiadito.platform.billing.interfaces.rest.transform.NameDirectory;
import pe.edu.upc.fiadito.platform.credit.domain.model.valueobjects.DebtStatus;
import pe.edu.upc.fiadito.platform.credit.domain.model.valueobjects.PaymentCalendar;
import pe.edu.upc.fiadito.platform.iam.application.internal.security.CurrentUser;
import pe.edu.upc.fiadito.platform.shared.application.time.SystemDateProvider;
import pe.edu.upc.fiadito.platform.shared.domain.model.events.OperationPerformedEvent;
import pe.edu.upc.fiadito.platform.shared.domain.model.valueobjects.Rounding;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Panel y reporte de corte", description = "Resumen de créditos y reporte de pago de cada corte")
public class ReportsController {

    private final BillingQueryService queryService;
    private final NameDirectory names;
    private final SystemDateProvider dates;
    private final ApplicationEventPublisher events;

    @Operation(summary = "Panel: resumen y plan de pagos de los créditos vigentes")
    @GetMapping("/panel")
    public PanelResource panel(@AuthenticationPrincipal CurrentUser user) {
        var today = dates.today();
        var obligations = queryService.getObligations(user.businessId());
        var pending = obligations.stream().filter(o -> !o.isPaid()).toList();
        var overdue = pending.stream().filter(o -> o.statusOn(today) == DebtStatus.EN_MORA).toList();
        var upcoming = pending.stream().filter(o -> !o.dueDate().isBefore(today)).toList();
        NextPaymentResource next = null;
        if (!upcoming.isEmpty()) {
            var date = upcoming.getFirst().dueDate();
            var due = upcoming.stream().filter(o -> o.dueDate().equals(date)).toList();
            next = new NextPaymentResource(date, sum(due), due.size());
        }
        var memberNames = names.memberNames(user.businessId());
        var productNames = names.productNames(user.businessId());
        var plan = obligations.stream().filter(o -> !o.sale().isPaid())
                .map(o -> BillingResourceAssembler.toResource(o, memberNames, productNames, today)).toList();
        int currentCredits = (int) pending.stream().map(o -> o.sale().getId()).distinct().count();
        return new PanelResource(sum(pending), currentCredits, sum(overdue), overdue.size(), next,
                new HashSet<>(overdue.stream().map(Obligation::memberId).toList()).size(),
                new HashSet<>(pending.stream().map(Obligation::memberId).toList()).size(), plan);
    }

    @Operation(summary = "Fechas de corte disponibles (días 10 y 25) y su fecha de pago")
    @GetMapping("/reportes/cortes")
    public List<CutoffOptionResource> cutoffs() {
        var today = dates.today();
        var current = PaymentCalendar.currentCutoff(today);
        return PaymentCalendar.cutoffsAround(today, 4, 4).stream()
                .map(c -> new CutoffOptionResource(c, PaymentCalendar.paymentDateOfCutoff(c), c.equals(current))).toList();
    }

    @Operation(summary = "Reporte de corte agrupado por socio (fecha = día 10 o 25; por defecto el corte vigente)")
    @GetMapping("/reportes/corte")
    public CutoffReportResource cutoffReport(@AuthenticationPrincipal CurrentUser user,
                                             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        var today = dates.today();
        var cutoff = fecha == null ? PaymentCalendar.currentCutoff(today) : fecha;
        var report = queryService.getCutoffReport(user.businessId(), cutoff);
        var memberNames = names.memberNames(user.businessId());
        var productNames = names.productNames(user.businessId());
        var groups = report.groups().stream().map(g -> new CutoffReportResource.Group(g.member().getId(), g.member().getDni(),
                g.member().getFullName(), g.member().getCurrency().name(), g.total(),
                g.items().stream().map(o -> BillingResourceAssembler.toResource(o, memberNames, productNames, today)).toList())).toList();
        events.publishEvent(new OperationPerformedEvent(user.businessId(), user.userId(), user.name(), "CONSULTAR", "VENTA", null,
                "Reporte de corte del " + cutoff.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))));
        return new CutoffReportResource(report.cutoffDate(), report.paymentDate(), report.preliminary(), report.total(), report.itemCount(), groups);
    }

    private static BigDecimal sum(List<Obligation> obligations) {
        return Rounding.money(obligations.stream().map(Obligation::amount).reduce(BigDecimal.ZERO, BigDecimal::add));
    }
}
