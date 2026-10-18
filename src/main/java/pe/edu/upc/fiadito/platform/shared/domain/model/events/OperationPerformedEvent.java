package pe.edu.upc.fiadito.platform.shared.domain.model.events;

/**
 * Evento de dominio que publica cada operación para que el contexto Audit la registre en BITACORA.
 *
 * @param businessId negocio de la operación (puede ser nulo en un login fallido con correo inexistente)
 * @param userId     usuario que operó (nulo si el login falló)
 * @param userName   nombre visible del usuario o correo intentado
 * @param action     LOGIN, LOGIN_FALLIDO, LOGOUT, REGISTRAR, EDITAR, VENDER, PAGAR, CONSULTAR
 * @param entity     tabla afectada (NEGOCIO, USUARIO, SOCIO, PRODUCTO, VENTA, PAGO)
 * @param recordId   id del registro afectado
 * @param detail     descripción breve
 */
public record OperationPerformedEvent(Long businessId, Long userId, String userName, String action,
                                      String entity, Long recordId, String detail) {
}
