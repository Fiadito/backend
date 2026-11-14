package pe.edu.upc.fiadito.platform.credit.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pe.edu.upc.fiadito.platform.catalog.interfaces.acl.CatalogContextFacade;
import pe.edu.upc.fiadito.platform.credit.domain.model.aggregates.CreditSale;
import pe.edu.upc.fiadito.platform.credit.domain.model.commands.ChangeSaleRateCommand;
import pe.edu.upc.fiadito.platform.credit.domain.model.commands.RegisterSaleCommand;
import pe.edu.upc.fiadito.platform.credit.domain.model.commands.SimulateSaleCommand;
import pe.edu.upc.fiadito.platform.credit.domain.services.CreditSaleCommandService;
import pe.edu.upc.fiadito.platform.credit.domain.services.CreditSaleQueryService;
import pe.edu.upc.fiadito.platform.credit.interfaces.rest.resources.*;
import pe.edu.upc.fiadito.platform.credit.interfaces.rest.transform.SaleResourceAssembler;
import pe.edu.upc.fiadito.platform.iam.application.internal.security.CurrentUser;
import pe.edu.upc.fiadito.platform.membership.domain.model.aggregates.Member;
import pe.edu.upc.fiadito.platform.membership.interfaces.acl.MembershipContextFacade;
import pe.edu.upc.fiadito.platform.shared.application.time.SystemDateProvider;
import pe.edu.upc.fiadito.platform.shared.domain.exceptions.ResourceNotFoundException;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Ventas", description = "Ventas al fiado en pago único o en cuotas quincenales iguales")
public class SalesController {

    private final CreditSaleCommandService commandService;
    private final CreditSaleQueryService queryService;
    private final MembershipContextFacade membership;
    private final CatalogContextFacade catalog;
    private final SystemDateProvider dates;

    @Operation(summary = "Calcula el monto, la fecha de pago y el plan de pagos sin guardar")
    @PostMapping("/ventas/simulacion")
    public SimulationResource simulate(@AuthenticationPrincipal CurrentUser user, @RequestBody SaleRequestResource body) {
        return SimulationResource.from(commandService.handle(new SimulateSaleCommand(user.businessId(), body.toRequest())));
    }

    @Operation(summary = "Registra la venta y su plan de pagos")
    @PostMapping("/ventas")
    public ResponseEntity<SaleResource> register(@AuthenticationPrincipal CurrentUser user, @RequestBody SaleRequestResource body) {
        var sale = commandService.handle(new RegisterSaleCommand(user.businessId(), body.toRequest()), user.userId(), user.name());
        return ResponseEntity.status(HttpStatus.CREATED).body(toResource(user.businessId(), sale));
    }

    @Operation(summary = "Cambia la tasa efectiva de la venta: las cuotas pendientes se recalculan desde su saldo")
    @PatchMapping("/ventas/{id}/tasa")
    public SaleResource changeRate(@AuthenticationPrincipal CurrentUser user, @PathVariable Long id, @RequestBody ChangeRateResource body) {
        var sale = commandService.handle(new ChangeSaleRateCommand(user.businessId(), id, body.periodoTasa(), body.valorTasa()),
                user.userId(), user.name());
        return toResource(user.businessId(), sale);
    }

    @Operation(summary = "Lista las ventas del negocio (opcional: de un socio)")
    @GetMapping("/ventas")
    public List<SaleResource> getAll(@AuthenticationPrincipal CurrentUser user, @RequestParam(required = false) Long idSocio) {
        var sales = idSocio == null ? queryService.getAllByBusiness(user.businessId()) : queryService.getAllByMember(user.businessId(), idSocio);
        var memberNames = memberNames(user.businessId());
        var productNames = catalog.productNames(user.businessId());
        return sales.stream().map(s -> SaleResourceAssembler.toResource(s, memberNames, productNames, dates.today())).toList();
    }

    @Operation(summary = "Obtiene una venta con sus cuotas")
    @GetMapping("/ventas/{id}")
    public SaleResource getById(@AuthenticationPrincipal CurrentUser user, @PathVariable Long id) {
        return toResource(user.businessId(), find(user, id));
    }

    @Operation(summary = "Plan de pago de una venta (el socio solo ve sus propias ventas)")
    @GetMapping("/ventas/{id}/cronograma")
    public List<InstallmentResource> schedule(@AuthenticationPrincipal CurrentUser user, @PathVariable Long id) {
        var sale = find(user, id);
        return sale.getInstallments().stream().map(i -> SaleResourceAssembler.toResource(i, sale, dates.today())).toList();
    }

    @Operation(summary = "Lista todas las cuotas del negocio (opcional: de un socio)")
    @GetMapping("/cuotas")
    public List<InstallmentResource> installments(@AuthenticationPrincipal CurrentUser user, @RequestParam(required = false) Long idSocio) {
        var sales = idSocio == null ? queryService.getAllByBusiness(user.businessId()) : queryService.getAllByMember(user.businessId(), idSocio);
        return sales.stream().flatMap(s -> s.getInstallments().stream().map(i -> SaleResourceAssembler.toResource(i, s, dates.today())))
                .sorted(Comparator.comparing(InstallmentResource::fechaVencimiento).thenComparing(InstallmentResource::idVenta))
                .toList();
    }

    private CreditSale find(CurrentUser user, Long id) {
        var sale = queryService.getById(user.businessId(), id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró la venta " + id + "."));
        if (user.isMember() && !sale.getMemberId().equals(user.memberId()))
            throw new ResourceNotFoundException("No se encontró la venta " + id + ".");
        return sale;
    }

    private SaleResource toResource(Long businessId, CreditSale sale) {
        return SaleResourceAssembler.toResource(sale, memberNames(businessId), catalog.productNames(businessId), dates.today());
    }

    private Map<Long, String> memberNames(Long businessId) {
        return membership.membersOf(businessId).stream().collect(Collectors.toMap(Member::getId, Member::getFullName));
    }
}
