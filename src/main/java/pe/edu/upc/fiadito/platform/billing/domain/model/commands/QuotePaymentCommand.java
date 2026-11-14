package pe.edu.upc.fiadito.platform.billing.domain.model.commands;

import pe.edu.upc.fiadito.platform.shared.domain.model.valueobjects.RatePeriod;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * POST /pagos/cotizacion: obligación (venta y cuota), fecha real de pago y, si está vencida, la tasa moratoria
 * (si no se envía, se usa TEA 60 %).
 */
public record QuotePaymentCommand(Long businessId, Long saleId, Long installmentId, LocalDate paymentDate,
                                  RatePeriod lateRatePeriod, BigDecimal lateRateValue) {
}
