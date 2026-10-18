package pe.edu.upc.fiadito.platform.catalog.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pe.edu.upc.fiadito.platform.catalog.domain.model.commands.ChangeProductStatusCommand;
import pe.edu.upc.fiadito.platform.catalog.domain.services.ProductCommandService;
import pe.edu.upc.fiadito.platform.catalog.domain.services.ProductQueryService;
import pe.edu.upc.fiadito.platform.catalog.interfaces.rest.resources.ProductResource;
import pe.edu.upc.fiadito.platform.catalog.interfaces.rest.resources.SaveProductResource;
import pe.edu.upc.fiadito.platform.iam.application.internal.security.CurrentUser;
import pe.edu.upc.fiadito.platform.membership.interfaces.rest.resources.ChangeStatusResource;

import java.util.List;

@RestController
@RequestMapping("/api/v1/productos")
@RequiredArgsConstructor
@Tag(name = "Productos", description = "Productos y servicios del negocio")
public class ProductsController {

    private final ProductCommandService commandService;
    private final ProductQueryService queryService;

    @Operation(summary = "Lista los productos del negocio")
    @GetMapping
    public List<ProductResource> getAll(@AuthenticationPrincipal CurrentUser user) {
        return queryService.getAllByBusiness(user.businessId()).stream().map(ProductResource::from).toList();
    }

    @Operation(summary = "Obtiene un producto")
    @GetMapping("/{id}")
    public ResponseEntity<ProductResource> getById(@AuthenticationPrincipal CurrentUser user, @PathVariable Long id) {
        return queryService.getById(user.businessId(), id).map(ProductResource::from)
                .map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Registra un producto")
    @PostMapping
    public ResponseEntity<ProductResource> create(@AuthenticationPrincipal CurrentUser user, @RequestBody SaveProductResource body) {
        var product = commandService.handle(body.toCommand(user.businessId(), null), user.userId(), user.name());
        return ResponseEntity.status(HttpStatus.CREATED).body(ProductResource.from(product));
    }

    @Operation(summary = "Edita un producto")
    @PutMapping("/{id}")
    public ProductResource update(@AuthenticationPrincipal CurrentUser user, @PathVariable Long id, @RequestBody SaveProductResource body) {
        return ProductResource.from(commandService.handle(body.toCommand(user.businessId(), id), user.userId(), user.name()));
    }

    @Operation(summary = "Da de baja o reactiva un producto")
    @PatchMapping("/{id}/estado")
    public ProductResource changeStatus(@AuthenticationPrincipal CurrentUser user, @PathVariable Long id,
                                        @Valid @RequestBody ChangeStatusResource body) {
        return ProductResource.from(commandService.handle(new ChangeProductStatusCommand(user.businessId(), id, body.estado()),
                user.userId(), user.name()));
    }
}
