package pe.edu.upc.fiadito.platform.iam.domain.model.commands;

/** CerrarSesion: solo se registra en la bitácora (el token se borra en el navegador). */
public record SignOutCommand(Long userId) {
}
