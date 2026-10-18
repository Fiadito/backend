package pe.edu.upc.fiadito.platform.iam.interfaces.rest.resources;

import pe.edu.upc.fiadito.platform.iam.domain.model.commands.RegisterBusinessCommand;

public record RegisterBusinessResource(String ruc, String nombreComercial, String giro, String direccion,
                                       String responsable, String correo, String clave, String confirmacion) {
    public RegisterBusinessCommand toCommand() {
        return new RegisterBusinessCommand(ruc == null ? "" : ruc.trim(), nombreComercial, giro, direccion, responsable, correo, clave, confirmacion);
    }
}
