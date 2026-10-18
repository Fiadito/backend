package pe.edu.upc.fiadito.platform.iam.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;

public record SignInResource(@NotBlank String correo, @NotBlank String clave) {
}
