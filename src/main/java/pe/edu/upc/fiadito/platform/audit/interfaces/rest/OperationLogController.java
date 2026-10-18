package pe.edu.upc.fiadito.platform.audit.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upc.fiadito.platform.audit.infrastructure.persistence.jpa.repositories.OperationLogRepository;
import pe.edu.upc.fiadito.platform.audit.interfaces.rest.resources.OperationLogResource;
import pe.edu.upc.fiadito.platform.iam.application.internal.security.CurrentUser;

import java.util.List;

@RestController
@RequestMapping("/api/v1/bitacora")
@RequiredArgsConstructor
@Tag(name = "Bitácora", description = "Registro de las operaciones del negocio")
public class OperationLogController {

    private final OperationLogRepository repository;

    @Operation(summary = "Últimas 500 operaciones del negocio (opcional: filtrar por acción)")
    @GetMapping
    @Transactional(readOnly = true)
    public List<OperationLogResource> getAll(@AuthenticationPrincipal CurrentUser user, @RequestParam(required = false) String accion) {
        var logs = accion == null || accion.isBlank()
                ? repository.findTop500ByBusinessIdOrderByIdDesc(user.businessId())
                : repository.findTop500ByBusinessIdAndActionOrderByIdDesc(user.businessId(), accion.toUpperCase());
        return logs.stream().map(OperationLogResource::from).toList();
    }
}
