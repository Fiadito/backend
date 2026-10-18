package pe.edu.upc.fiadito.platform.iam.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pe.edu.upc.fiadito.platform.iam.application.internal.security.CurrentUser;
import pe.edu.upc.fiadito.platform.iam.domain.model.commands.SignInCommand;
import pe.edu.upc.fiadito.platform.iam.domain.model.commands.SignOutCommand;
import pe.edu.upc.fiadito.platform.iam.domain.services.IamCommandService;
import pe.edu.upc.fiadito.platform.iam.interfaces.rest.resources.*;
import pe.edu.upc.fiadito.platform.iam.interfaces.rest.transform.AuthenticatedUserResourceAssembler;
import pe.edu.upc.fiadito.platform.shared.interfaces.rest.resources.MessageResource;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Registro del negocio, inicio y cierre de sesión")
public class AuthController {

    private final IamCommandService commandService;

    @Operation(summary = "Registra el negocio y su usuario (rol NEGOCIO)")
    @PostMapping("/registro-negocio")
    public ResponseEntity<BusinessResource> registerBusiness(@RequestBody RegisterBusinessResource body) {
        var business = commandService.handle(body.toCommand());
        return ResponseEntity.status(HttpStatus.CREATED).body(BusinessResource.from(business));
    }

    @Operation(summary = "Valida correo y contraseña y devuelve el token JWT")
    @PostMapping("/login")
    public ResponseEntity<AuthenticatedUserResource> signIn(@Valid @RequestBody SignInResource body) {
        var authenticated = commandService.handle(new SignInCommand(body.correo(), body.clave()));
        return ResponseEntity.ok(AuthenticatedUserResourceAssembler.toResource(authenticated));
    }

    @Operation(summary = "Registra el cierre de sesión en la bitácora")
    @PostMapping("/logout")
    public ResponseEntity<MessageResource> signOut(@AuthenticationPrincipal CurrentUser user) {
        commandService.handle(new SignOutCommand(user.userId()));
        return ResponseEntity.ok(new MessageResource("Sesión cerrada."));
    }
}
