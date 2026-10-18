package pe.edu.upc.fiadito.platform.billing.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pe.edu.upc.fiadito.platform.billing.domain.model.commands.QuotePaymentCommand;
import pe.edu.upc.fiadito.platform.billing.domain.services.BillingQueryService;
import pe.edu.upc.fiadito.platform.billing.domain.services.PaymentCommandService;
import pe.edu.upc.fiadito.platform.billing.interfaces.rest.resources.*;
import pe.edu.upc.fiadito.platform.billing.interfaces.rest.transform.BillingResourceAssembler;
import pe.edu.upc.fiadito.platform.billing.interfaces.rest.transform.NameDirectory;
import pe.edu.upc.fiadito.platform.iam.application.internal.security.CurrentUser;
import pe.edu.upc.fiadito.platform.membership.interfaces.acl.MembershipContextFacade;
import pe.edu.upc.fiadito.platform.shared.application.time.SystemDateProvider;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Pagos", description = "Obligaciones pendientes, cotización con interés moratorio y registro de pagos")
public class PaymentsController {

    private final PaymentCommandService commandService;
    private final BillingQueryService queryService;
    private final MembershipContextFacade membership;
    private final NameDirectory names;
    private final SystemDateProvider dates;

    @Operation(summary = "Lista las obligaciones del negocio (estado=PENDIENTE para las que faltan pagar)")
    @GetMapping("/obligaciones")
    public List<ObligationResource> obligations(@AuthenticationPrincipal CurrentUser user, @RequestParam(required = false) String estado) {
        var memberNames = names.memberNames(user.businessId());
        var productNames = names.productNames(user.businessId());
        var today = dates.today();
        return queryService.getObligations(user.businessId()).stream()
                .filter(o -> estado == null || ("PENDIENTE".equalsIgnoreCase(estado) ? !o.isPaid() : o.statusOn(today).name().equalsIgnoreCase(estado)))
                .map(o -> BillingResourceAssembler.toResource(o, memberNames, productNames, today)).toList();
    }

    @Operation(summary = "Calcula días de mora, interés moratorio, total exigible e imputación")
    @PostMapping("/pagos/cotizacion")
    public QuoteResource quote(@AuthenticationPrincipal CurrentUser user, @RequestBody QuoteRequestResource body) {
        var quoted = commandService.handle(new QuotePaymentCommand(user.businessId(), body.idVenta(), body.idCuota(), body.fechaPagoReal()));
        var o = quoted.obligation();
        var q = quoted.quote();
        var member = membership.findMember(user.businessId(), o.memberId()).orElseThrow();
        return new QuoteResource(o.sale().getId(), o.installment() == null ? null : o.installment().getId(), member.getFullName(), o.label(),
                o.dueDate(), body.fechaPagoReal(), q.amount(), q.lateDays(), member.getLateRatePeriod().name(), member.getLateRateValue(),
                q.dailyRate(), q.lateInterest(), q.total(), q.lateImputation(), q.capitalImputation());
    }

    @Operation(summary = "Registra el pago completo de una obligación (no acepta pagos parciales)")
    @PostMapping("/pagos")
    public ResponseEntity<PaymentResource> register(@AuthenticationPrincipal CurrentUser user, @RequestBody RegisterPaymentResource body) {
        var payment = commandService.handle(body.toCommand(user.businessId()), user.userId(), user.name());
        var resource = BillingResourceAssembler.toResource(payment, queryService.getObligations(user.businessId()),
                names.memberNames(user.businessId()), names.productNames(user.businessId()));
        return ResponseEntity.status(HttpStatus.CREATED).body(resource);
    }

    @Operation(summary = "Lista los pagos registrados")
    @GetMapping("/pagos")
    public List<PaymentResource> payments(@AuthenticationPrincipal CurrentUser user) {
        var obligations = queryService.getObligations(user.businessId());
        var memberNames = names.memberNames(user.businessId());
        var productNames = names.productNames(user.businessId());
        return queryService.getPayments(user.businessId()).stream()
                .map(p -> BillingResourceAssembler.toResource(p, obligations, memberNames, productNames)).toList();
    }
}
