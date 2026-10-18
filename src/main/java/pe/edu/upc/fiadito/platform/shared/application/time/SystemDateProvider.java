package pe.edu.upc.fiadito.platform.shared.application.time;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;

/**
 * Fuente del "hoy" del sistema (zona horaria de Lima).
 * Con app.system-date=2026-11-04 se simula otra fecha para ensayar los casos de prueba,
 * igual que VITE_SYSTEM_DATE en el frontend.
 */
@Component
public class SystemDateProvider {

    private final ZoneId zone;
    private final LocalDate simulatedDate;

    public SystemDateProvider(@Value("${app.zone:America/Lima}") String zone,
                              @Value("${app.system-date:}") String simulatedDate) {
        this.zone = ZoneId.of(zone);
        this.simulatedDate = simulatedDate == null || simulatedDate.isBlank() ? null : LocalDate.parse(simulatedDate.trim());
    }

    public LocalDate today() {
        return simulatedDate != null ? simulatedDate : LocalDate.now(zone);
    }

    public LocalDateTime now() {
        return LocalDateTime.of(today(), LocalTime.now(zone).withNano(0));
    }
}
