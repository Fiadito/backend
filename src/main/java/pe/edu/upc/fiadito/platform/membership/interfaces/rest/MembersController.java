package pe.edu.upc.fiadito.platform.membership.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pe.edu.upc.fiadito.platform.billing.interfaces.acl.BillingContextFacade;
import pe.edu.upc.fiadito.platform.iam.application.internal.security.CurrentUser;
import pe.edu.upc.fiadito.platform.iam.interfaces.acl.IamContextFacade;
import pe.edu.upc.fiadito.platform.membership.domain.model.aggregates.Member;
import pe.edu.upc.fiadito.platform.membership.domain.model.commands.ChangeMemberStatusCommand;
import pe.edu.upc.fiadito.platform.membership.domain.services.MemberCommandService;
import pe.edu.upc.fiadito.platform.membership.domain.services.MemberQueryService;
import pe.edu.upc.fiadito.platform.membership.interfaces.rest.resources.ChangeStatusResource;
import pe.edu.upc.fiadito.platform.membership.interfaces.rest.resources.MemberResource;
import pe.edu.upc.fiadito.platform.membership.interfaces.rest.resources.SaveMemberResource;
import pe.edu.upc.fiadito.platform.membership.interfaces.rest.transform.MemberResourceAssembler;

import java.util.List;

@RestController
@RequestMapping("/api/v1/socios")
@RequiredArgsConstructor
@Tag(name = "Socios", description = "Socios y sus condiciones de crédito")
public class MembersController {

    private final MemberCommandService commandService;
    private final MemberQueryService queryService;
    private final BillingContextFacade billing;
    private final IamContextFacade iam;

    @Operation(summary = "Lista los socios con su deuda, crédito disponible y mora")
    @GetMapping
    public List<MemberResource> getAll(@AuthenticationPrincipal CurrentUser user) {
        return queryService.getAllByBusiness(user.businessId()).stream().map(m -> toResource(user.businessId(), m)).toList();
    }

    @Operation(summary = "Obtiene un socio")
    @GetMapping("/{id}")
    public ResponseEntity<MemberResource> getById(@AuthenticationPrincipal CurrentUser user, @PathVariable Long id) {
        return queryService.getById(user.businessId(), id).map(m -> toResource(user.businessId(), m))
                .map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Registra un socio (y su cuenta si se envían correo y clave)")
    @PostMapping
    public ResponseEntity<MemberResource> create(@AuthenticationPrincipal CurrentUser user, @RequestBody SaveMemberResource body) {
        var member = commandService.handle(body.toCreateCommand(user.businessId()), user.userId(), user.name());
        return ResponseEntity.status(HttpStatus.CREATED).body(toResource(user.businessId(), member));
    }

    @Operation(summary = "Edita un socio")
    @PutMapping("/{id}")
    public MemberResource update(@AuthenticationPrincipal CurrentUser user, @PathVariable Long id, @RequestBody SaveMemberResource body) {
        return toResource(user.businessId(), commandService.handle(body.toUpdateCommand(user.businessId(), id), user.userId(), user.name()));
    }

    @Operation(summary = "Da de baja (si no tiene deuda) o reactiva un socio")
    @PatchMapping("/{id}/estado")
    public MemberResource changeStatus(@AuthenticationPrincipal CurrentUser user, @PathVariable Long id, @Valid @RequestBody ChangeStatusResource body) {
        return toResource(user.businessId(), commandService.handle(new ChangeMemberStatusCommand(user.businessId(), id, body.estado()),
                user.userId(), user.name()));
    }

    private MemberResource toResource(Long businessId, Member member) {
        var debt = billing.debtSummaryOf(businessId, member.getId());
        return MemberResourceAssembler.toResource(member, iam.emailOfMember(member.getId()), debt.capitalDebt(), debt.availableCredit(), debt.overdue());
    }
}
