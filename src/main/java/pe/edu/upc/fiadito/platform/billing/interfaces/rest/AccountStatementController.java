package pe.edu.upc.fiadito.platform.billing.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upc.fiadito.platform.billing.domain.model.readmodels.AccountStatement;
import pe.edu.upc.fiadito.platform.billing.domain.services.BillingQueryService;
import pe.edu.upc.fiadito.platform.billing.interfaces.rest.resources.AccountStatementResource;
import pe.edu.upc.fiadito.platform.billing.interfaces.rest.resources.NextPaymentResource;
import pe.edu.upc.fiadito.platform.billing.interfaces.rest.transform.BillingResourceAssembler;
import pe.edu.upc.fiadito.platform.billing.interfaces.rest.transform.NameDirectory;
import pe.edu.upc.fiadito.platform.credit.interfaces.rest.transform.SaleResourceAssembler;
import pe.edu.upc.fiadito.platform.iam.application.internal.security.CurrentUser;
import pe.edu.upc.fiadito.platform.iam.interfaces.acl.IamContextFacade;
import pe.edu.upc.fiadito.platform.membership.interfaces.rest.transform.MemberResourceAssembler;

@RestController
@RequestMapping("/api/v1/socios")
@RequiredArgsConstructor
@Tag(name = "Estado de cuenta", description = "Estado de cuenta del socio (solo consulta)")
public class AccountStatementController {

    private final BillingQueryService queryService;
    private final NameDirectory names;
    private final IamContextFacade iam;

    @Operation(summary = "Estado de cuenta del socio autenticado (rol SOCIO)")
    @GetMapping("/me/estado-cuenta")
    public AccountStatementResource mine(@AuthenticationPrincipal CurrentUser user) {
        return toResource(user.businessId(), queryService.getAccountStatement(user.businessId(), user.memberId()));
    }

    @Operation(summary = "Estado de cuenta y plan de pago de un socio (rol NEGOCIO)")
    @GetMapping("/{id}/estado-cuenta")
    public AccountStatementResource ofMember(@AuthenticationPrincipal CurrentUser user, @PathVariable Long id) {
        return toResource(user.businessId(), queryService.getAccountStatement(user.businessId(), id));
    }

    private AccountStatementResource toResource(Long businessId, AccountStatement s) {
        var memberNames = names.memberNames(businessId);
        var productNames = names.productNames(businessId);
        var today = s.today();
        var m = s.member();
        var next = s.nextPayment();
        return new AccountStatementResource(
                MemberResourceAssembler.toResource(m, iam.emailOfMember(m.getId()), s.capitalDebt(), s.availableCredit(), s.isOverdue()),
                today, next == null ? null : new NextPaymentResource(next.date(), next.amount(), next.count()),
                s.capitalDebt(), s.pendingInterest(), s.lateInterestToday(), s.debtToday(), s.availableCredit(), s.isOverdue(),
                s.sales().stream().map(sale -> SaleResourceAssembler.toResource(sale, memberNames, productNames, today)).toList(),
                s.obligations().stream().map(o -> BillingResourceAssembler.toResource(o, memberNames, productNames, today)).toList(),
                s.pending().stream().map(p -> BillingResourceAssembler.toResource(p, memberNames, productNames, today)).toList(),
                s.payments().stream().map(p -> BillingResourceAssembler.toResource(p, s.obligations(), memberNames, productNames)).toList());
    }
}
