package pe.edu.upc.fiadito.platform.billing.interfaces.rest.resources;

import pe.edu.upc.fiadito.platform.billing.domain.model.commands.RegisterPaymentCommand;
import pe.edu.upc.fiadito.platform.billing.domain.model.valueobjects.PaymentMethod;
import pe.edu.upc.fiadito.platform.shared.domain.model.valueobjects.RatePeriod;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Cuerpo de POST /pagos. La tasa moratoria solo se usa si la obligación está vencida (por defecto TEA 60 %). */
public record RegisterPaymentResource(Long idVenta, Long idCuota, LocalDate fechaPagoReal, PaymentMethod medioPago, BigDecimal montoPagado,
                                      RatePeriod periodoTasaMora, BigDecimal valorTasaMora) {
    public RegisterPaymentCommand toCommand(Long businessId) {
        return new RegisterPaymentCommand(businessId, idVenta, idCuota, fechaPagoReal, medioPago, montoPagado, periodoTasaMora, valorTasaMora);
    }
}
