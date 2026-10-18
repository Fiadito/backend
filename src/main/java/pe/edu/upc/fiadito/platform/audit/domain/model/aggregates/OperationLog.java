package pe.edu.upc.fiadito.platform.audit.domain.model.aggregates;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Agregado BITACORA: rastro de quién hizo qué y cuándo.
 */
@Entity
@Table(name = "bitacora", indexes = @Index(name = "ix_bitacora_negocio_fecha", columnList = "id_negocio, fecha_hora"))
@Getter
@NoArgsConstructor
public class OperationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_bitacora")
    private Long id;

    @Column(name = "id_negocio")
    private Long businessId;

    /** Usuario que operó (nulo si el login falló). */
    @Column(name = "id_usuario")
    private Long userId;

    @Column(name = "usuario", length = 100)
    private String userName;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime occurredAt;

    @Column(name = "accion", nullable = false, length = 20)
    private String action;

    @Column(name = "entidad", nullable = false, length = 20)
    private String entity;

    @Column(name = "id_registro")
    private Long recordId;

    @Column(name = "detalle", length = 255)
    private String detail;

    @Column(name = "ip", length = 45)
    private String ip;

    public OperationLog(Long businessId, Long userId, String userName, LocalDateTime occurredAt, String action, String entity,
                        Long recordId, String detail, String ip) {
        this.businessId = businessId;
        this.userId = userId;
        this.userName = userName;
        this.occurredAt = occurredAt;
        this.action = action;
        this.entity = entity;
        this.recordId = recordId;
        this.detail = detail != null && detail.length() > 255 ? detail.substring(0, 255) : detail;
        this.ip = ip;
    }
}
