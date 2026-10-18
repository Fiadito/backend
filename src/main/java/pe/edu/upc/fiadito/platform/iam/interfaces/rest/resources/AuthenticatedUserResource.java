package pe.edu.upc.fiadito.platform.iam.interfaces.rest.resources;

public record AuthenticatedUserResource(String token, UserResource usuario) {
}
