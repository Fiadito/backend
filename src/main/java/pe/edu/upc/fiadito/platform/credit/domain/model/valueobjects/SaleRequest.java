package pe.edu.upc.fiadito.platform.credit.domain.model.valueobjects;

import pe.edu.upc.fiadito.platform.shared.domain.model.valueobjects.RatePeriod;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Datos de la compra: socio, producto o combo, cantidad, fechaHoraCompra, modalidad, nCuotas y la tasa efectiva de la venta
 * (periodo y valor, TEA equivalente entre 15 % y 27 %).
 */
public record SaleRequest(Long memberId, Long productId, Integer quantity, LocalDateTime purchaseDateTime,
                          PaymentMode paymentMode, Integer installmentCount, RatePeriod ratePeriod, BigDecimal rateValue) {
}
