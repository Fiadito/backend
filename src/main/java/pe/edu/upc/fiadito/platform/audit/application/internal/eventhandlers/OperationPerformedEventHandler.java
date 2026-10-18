package pe.edu.upc.fiadito.platform.audit.application.internal.eventhandlers;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import pe.edu.upc.fiadito.platform.audit.domain.model.aggregates.OperationLog;
import pe.edu.upc.fiadito.platform.audit.infrastructure.persistence.jpa.repositories.OperationLogRepository;
import pe.edu.upc.fiadito.platform.shared.application.time.SystemDateProvider;
import pe.edu.upc.fiadito.platform.shared.domain.model.events.OperationPerformedEvent;

/**
 * Registra cada operación en BITACORA dentro de la misma transacción de la operación
 * (si la operación falla, tampoco queda el registro).
 */
@Service
@RequiredArgsConstructor
public class OperationPerformedEventHandler {

    private final OperationLogRepository repository;
    private final SystemDateProvider dates;

    @EventListener
    @Transactional
    public void on(OperationPerformedEvent event) {
        repository.save(new OperationLog(event.businessId(), event.userId(), event.userName(), dates.now(), event.action(),
                event.entity(), event.recordId(), event.detail(), clientIp()));
    }

    private static String clientIp() {
        if (!(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes)) return "sistema";
        HttpServletRequest request = attributes.getRequest();
        var forwarded = request.getHeader("X-Forwarded-For");
        return forwarded != null && !forwarded.isBlank() ? forwarded.split(",")[0].trim() : request.getRemoteAddr();
    }
}
