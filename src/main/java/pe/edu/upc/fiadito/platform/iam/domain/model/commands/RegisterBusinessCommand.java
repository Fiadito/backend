package pe.edu.upc.fiadito.platform.iam.domain.model.commands;

/** RegistrarNegocio: datos del negocio y de su cuenta (módulo 7.2). */
public record RegisterBusinessCommand(String ruc, String name, String line, String address,
                                      String responsible, String email, String password, String confirmation) {
}
