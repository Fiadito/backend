package pe.edu.upc.fiadito.platform.billing.domain.model.commands;

import pe.edu.upc.fiadito.platform.billing.domain.model.valueobjects.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDate;

/** RegistrarPago: LEER obligacion, fechaReal, medioPago, montoPagado (módulo 7.9). */
public record RegisterPaymentCommand(Long businessId, Long saleId, Long installmentId, LocalDate paymentDate, PaymentMethod method,
                                     BigDecimal paid) {
}
