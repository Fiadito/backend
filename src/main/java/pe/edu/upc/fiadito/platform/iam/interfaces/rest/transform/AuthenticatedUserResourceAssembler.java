package pe.edu.upc.fiadito.platform.iam.interfaces.rest.transform;

import pe.edu.upc.fiadito.platform.iam.domain.model.valueobjects.AuthenticatedUser;
import pe.edu.upc.fiadito.platform.iam.interfaces.rest.resources.AuthenticatedUserResource;
import pe.edu.upc.fiadito.platform.iam.interfaces.rest.resources.UserResource;

public class AuthenticatedUserResourceAssembler {
    public static AuthenticatedUserResource toResource(AuthenticatedUser a) {
        var u = a.user();
        return new AuthenticatedUserResource(a.token(), new UserResource(u.getId(), u.getBusinessId(), u.getMemberId(),
                u.getName(), u.getEmail(), u.getRole().name(), a.business().getName()));
    }
}
