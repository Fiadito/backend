package pe.edu.upc.fiadito.platform.iam.application.internal.security;

import pe.edu.upc.fiadito.platform.iam.domain.model.valueobjects.Role;

/**
 * Usuario autenticado, armado desde los claims del JWT. Cada consulta se filtra por su businessId
 * (un negocio solo ve sus datos) y, si es SOCIO, por su memberId.
 */
public record CurrentUser(Long userId, Long businessId, Long memberId, Role role, String name, String email) {

    public boolean isMember() {
        return role == Role.SOCIO;
    }
}
