package pe.edu.upc.fiadito.platform.credit.interfaces.rest.resources;

import pe.edu.upc.fiadito.platform.credit.domain.model.valueobjects.PaymentMode;
import pe.edu.upc.fiadito.platform.credit.domain.model.valueobjects.SaleRequest;
import pe.edu.upc.fiadito.platform.shared.domain.model.valueobjects.RatePeriod;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Cuerpo de POST /ventas y /ventas/simulacion. fechaHoraCompra: "2026-10-05T10:00".
 * periodoTasa y valorTasa: tasa efectiva de la venta (valorTasa como fracción, 0.20 = 20 %).
 */
public record SaleRequestResource(Long idSocio, Long idProducto, Integer cantidad, LocalDateTime fechaHoraCompra,
                                  PaymentMode modalidad, Integer nCuotas, RatePeriod periodoTasa, BigDecimal valorTasa) {
    public SaleRequest toRequest() {
        return new SaleRequest(idSocio, idProducto, cantidad, fechaHoraCompra == null ? null : fechaHoraCompra.withSecond(0).withNano(0),
                modalidad, nCuotas, periodoTasa, valorTasa);
    }
}
