package pe.edu.upc.fiadito.platform.iam.domain.model.commands;

/** IniciarSesion(correo, contraseña). */
public record SignInCommand(String email, String password) {
}
