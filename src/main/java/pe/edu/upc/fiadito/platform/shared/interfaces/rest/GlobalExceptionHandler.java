package pe.edu.upc.fiadito.platform.shared.interfaces.rest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import pe.edu.upc.fiadito.platform.shared.domain.exceptions.BusinessRuleException;
import pe.edu.upc.fiadito.platform.shared.domain.exceptions.ConflictException;
import pe.edu.upc.fiadito.platform.shared.domain.exceptions.ResourceNotFoundException;
import pe.edu.upc.fiadito.platform.shared.interfaces.rest.resources.MessageResource;

/**
 * Convierte las excepciones en respuestas JSON {"message": "..."} con el código HTTP adecuado.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({BusinessRuleException.class, IllegalArgumentException.class})
    public ResponseEntity<MessageResource> badRequest(RuntimeException ex) {
        return ResponseEntity.badRequest().body(new MessageResource(ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<MessageResource> invalid(MethodArgumentNotValidException ex) {
        var error = ex.getBindingResult().getFieldErrors().stream().findFirst()
                .map(field -> field.getField() + ": " + field.getDefaultMessage()).orElse("Datos inválidos.");
        return ResponseEntity.badRequest().body(new MessageResource(error));
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<MessageResource> unreadable(Exception ex) {
        return ResponseEntity.badRequest().body(new MessageResource("El formato de los datos enviados no es válido."));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<MessageResource> notFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResource(ex.getMessage()));
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<MessageResource> conflict(ConflictException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new MessageResource(ex.getMessage()));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<MessageResource> badCredentials(BadCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new MessageResource("Correo o contraseña incorrectos."));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<MessageResource> forbidden(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new MessageResource("Acceso denegado para tu rol."));
    }
}
