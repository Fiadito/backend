package pe.edu.upc.fiadito.platform.shared.domain.exceptions;

/** El dato ya existe: correo, RUC o DNI repetido (HTTP 409). */
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
