package pe.edu.upc.fiadito.platform.iam.interfaces.rest.resources;

/** Datos de la sesión que guarda el frontend (sin la contraseña). */
public record UserResource(Long id, Long idNegocio, Long idSocio, String nombre, String correo, String rol, String nombreNegocio) {
}
