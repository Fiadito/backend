package pe.edu.upc.fiadito.platform.billing.domain.model.commands;

import java.time.LocalDate;

/** POST /pagos/cotizacion: obligación (venta y cuota) y fecha real de pago. */
public record QuotePaymentCommand(Long businessId, Long saleId, Long installmentId, LocalDate paymentDate) {
}
