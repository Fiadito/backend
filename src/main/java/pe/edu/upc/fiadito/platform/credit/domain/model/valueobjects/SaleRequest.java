package pe.edu.upc.fiadito.platform.credit.domain.model.valueobjects;

import java.time.LocalDateTime;

/** Datos de la compra: LEER socio, producto, cantidad, fechaHoraCompra, modalidad, nCuotas (módulo 7.6). */
public record SaleRequest(Long memberId, Long productId, Integer quantity, LocalDateTime purchaseDateTime,
                          PaymentMode paymentMode, Integer installmentCount) {
}
