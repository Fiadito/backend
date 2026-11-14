package pe.edu.upc.fiadito.platform.credit.domain.model.commands;

import pe.edu.upc.fiadito.platform.shared.domain.model.valueobjects.RatePeriod;

import java.math.BigDecimal;

/** PATCH /ventas/{id}/tasa: nueva tasa efectiva de la venta (recalcula las cuotas pendientes). */
public record ChangeSaleRateCommand(Long businessId, Long saleId, RatePeriod ratePeriod, BigDecimal rateValue) {
}
