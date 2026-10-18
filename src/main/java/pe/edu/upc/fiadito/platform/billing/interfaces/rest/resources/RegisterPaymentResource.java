package pe.edu.upc.fiadito.platform.billing.interfaces.rest.resources;

import pe.edu.upc.fiadito.platform.billing.domain.model.commands.RegisterPaymentCommand;
import pe.edu.upc.fiadito.platform.billing.domain.model.valueobjects.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RegisterPaymentResource(Long idVenta, Long idCuota, LocalDate fechaPagoReal, PaymentMethod medioPago, BigDecimal montoPagado) {
    public RegisterPaymentCommand toCommand(Long businessId) {
        return new RegisterPaymentCommand(businessId, idVenta, idCuota, fechaPagoReal, medioPago, montoPagado);
    }
}
