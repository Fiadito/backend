package pe.edu.upc.fiadito.platform.shared.domain.exceptions;

/** El recurso no existe o no pertenece al negocio del usuario (HTTP 404). */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
