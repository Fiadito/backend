package pe.edu.upc.fiadito.platform.membership.domain.model.commands;

import pe.edu.upc.fiadito.platform.membership.domain.model.valueobjects.Currency;

import java.math.BigDecimal;

/** Registro de un socio con su cuenta de acceso opcional (correo y contraseña inicial). Las tasas van en cada venta. */
public record CreateMemberCommand(Long businessId, String dni, String fullName, String phone, String address, Currency currency,
                                  BigDecimal creditLimit, Integer maxInstallments, String email, String password) {
}
