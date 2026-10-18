package pe.edu.upc.fiadito.platform.shared.domain.exceptions;

/** Una regla del negocio no se cumple (HTTP 400). */
public class BusinessRuleException extends RuntimeException {
    public BusinessRuleException(String message) {
        super(message);
    }
}
