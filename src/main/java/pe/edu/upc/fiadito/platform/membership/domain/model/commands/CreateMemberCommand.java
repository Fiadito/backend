package pe.edu.upc.fiadito.platform.membership.domain.model.commands;

import pe.edu.upc.fiadito.platform.membership.domain.model.valueobjects.Currency;
import pe.edu.upc.fiadito.platform.shared.domain.model.valueobjects.RatePeriod;

import java.math.BigDecimal;

/** Registro de un socio con su cuenta de acceso opcional (correo y contraseña inicial). */
public record CreateMemberCommand(Long businessId, String dni, String fullName, String phone, String address, Currency currency,
                                  RatePeriod lateRatePeriod, BigDecimal lateRateValue, BigDecimal creditLimit, Integer maxInstallments,
                                  String email, String password) {
}
