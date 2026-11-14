package pe.edu.upc.fiadito.platform.credit.interfaces.rest.resources;

import pe.edu.upc.fiadito.platform.shared.domain.model.valueobjects.RatePeriod;

import java.math.BigDecimal;

/** Cuerpo de PATCH /ventas/{id}/tasa: {"periodoTasa": "TEA", "valorTasa": 0.27}. */
public record ChangeRateResource(RatePeriod periodoTasa, BigDecimal valorTasa) {
}
