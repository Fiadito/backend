package pe.edu.upc.fiadito.platform.credit.interfaces.rest.resources;

import pe.edu.upc.fiadito.platform.credit.domain.model.valueobjects.PaymentMode;
import pe.edu.upc.fiadito.platform.credit.domain.model.valueobjects.SaleRequest;

import java.time.LocalDateTime;

/** Cuerpo de POST /ventas y /ventas/simulacion. fechaHoraCompra: "2026-10-05T10:00". */
public record SaleRequestResource(Long idSocio, Long idProducto, Integer cantidad, LocalDateTime fechaHoraCompra,
                                  PaymentMode modalidad, Integer nCuotas) {
    public SaleRequest toRequest() {
        return new SaleRequest(idSocio, idProducto, cantidad, fechaHoraCompra == null ? null : fechaHoraCompra.withSecond(0).withNano(0),
                modalidad, nCuotas);
    }
}
