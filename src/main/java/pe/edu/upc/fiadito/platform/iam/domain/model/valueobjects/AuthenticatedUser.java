package pe.edu.upc.fiadito.platform.iam.domain.model.valueobjects;

import pe.edu.upc.fiadito.platform.iam.domain.model.aggregates.Business;
import pe.edu.upc.fiadito.platform.iam.domain.model.aggregates.User;

/** Resultado del inicio de sesión: usuario, su negocio y el token JWT. */
public record AuthenticatedUser(User user, Business business, String token) {
}
