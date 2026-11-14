package pe.edu.upc.fiadito.platform.membership.interfaces.rest.resources;

import pe.edu.upc.fiadito.platform.membership.domain.model.commands.CreateMemberCommand;
import pe.edu.upc.fiadito.platform.membership.domain.model.commands.UpdateMemberCommand;
import pe.edu.upc.fiadito.platform.membership.domain.model.valueobjects.Currency;

import java.math.BigDecimal;

/**
 * Datos del formulario Registrar / Editar socio. correo y clave solo se usan al registrar.
 */
public record SaveMemberResource(String dni, String nombres, String celular, String direccion, Currency moneda,
                                 BigDecimal limiteCredito, Integer maxCuotas, String correo, String clave) {

    public CreateMemberCommand toCreateCommand(Long businessId) {
        return new CreateMemberCommand(businessId, dni == null ? null : dni.trim(), nombres, celular, direccion, moneda,
                limiteCredito, maxCuotas, correo, clave);
    }

    public UpdateMemberCommand toUpdateCommand(Long businessId, Long memberId) {
        return new UpdateMemberCommand(businessId, memberId, dni == null ? null : dni.trim(), nombres, celular, direccion, moneda,
                limiteCredito, maxCuotas);
    }
}
