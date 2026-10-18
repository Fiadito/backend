package pe.edu.upc.fiadito.platform.credit.domain.model.valueobjects;

/** Estado guardado de una venta o cuota. EN_MORA se calcula al consultar (fecha vencida y no pagada). */
public enum DebtStatus {
    PENDIENTE, PAGADA, EN_MORA
}
