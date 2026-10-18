package pe.edu.upc.fiadito.platform.audit.interfaces.rest.resources;

import com.fasterxml.jackson.annotation.JsonFormat;
import pe.edu.upc.fiadito.platform.audit.domain.model.aggregates.OperationLog;

import java.time.LocalDateTime;

public record OperationLogResource(Long id, Long idNegocio, Long idUsuario, String usuario,
                                   @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime fechaHora,
                                   String accion, String entidad, Long idRegistro, String detalle, String ip) {
    public static OperationLogResource from(OperationLog o) {
        return new OperationLogResource(o.getId(), o.getBusinessId(), o.getUserId(), o.getUserName(), o.getOccurredAt(), o.getAction(),
                o.getEntity(), o.getRecordId(), o.getDetail(), o.getIp());
    }
}
