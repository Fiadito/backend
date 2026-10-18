package pe.edu.upc.fiadito.platform.iam.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upc.fiadito.platform.iam.application.internal.security.CurrentUser;
import pe.edu.upc.fiadito.platform.iam.domain.services.IamQueryService;
import pe.edu.upc.fiadito.platform.iam.interfaces.rest.resources.BusinessResource;

@RestController
@RequestMapping("/api/v1/negocios")
@RequiredArgsConstructor
@Tag(name = "Negocio", description = "Datos del negocio del usuario autenticado")
public class BusinessController {

    private final IamQueryService queryService;

    @Operation(summary = "Negocio del usuario autenticado")
    @GetMapping("/me")
    public ResponseEntity<BusinessResource> me(@AuthenticationPrincipal CurrentUser user) {
        return queryService.getBusinessById(user.businessId()).map(BusinessResource::from)
                .map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
}
