package pe.edu.upc.fiadito.platform.billing.domain.model.commands;

import pe.edu.upc.fiadito.platform.billing.domain.model.valueobjects.PaymentMethod;
import pe.edu.upc.fiadito.platform.shared.domain.model.valueobjects.RatePeriod;

import java.math.BigDecimal;
import java.time.LocalDate;

/** RegistrarPago: obligación, fecha real, medio de pago, monto pagado y la tasa moratoria si está vencida (módulo 7.9). */
public record RegisterPaymentCommand(Long businessId, Long saleId, Long installmentId, LocalDate paymentDate, PaymentMethod method,
                                     BigDecimal paid, RatePeriod lateRatePeriod, BigDecimal lateRateValue) {
}
