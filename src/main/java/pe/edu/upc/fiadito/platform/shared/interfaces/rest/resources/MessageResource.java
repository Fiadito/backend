package pe.edu.upc.fiadito.platform.shared.interfaces.rest.resources;

/** Respuesta con un mensaje (el frontend lee el campo "message"). */
public record MessageResource(String message) {
}
