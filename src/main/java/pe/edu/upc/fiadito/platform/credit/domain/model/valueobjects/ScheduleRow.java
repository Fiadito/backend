package pe.edu.upc.fiadito.platform.credit.domain.model.valueobjects;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Fila del plan de pagos (Tabla 2: n_cuota, fecha_vencimiento, saldo_inicial, amortizacion, cuota, saldo_final).
 */
public record ScheduleRow(int number, LocalDate dueDate, BigDecimal openingBalance, BigDecimal amortization,
                          BigDecimal amount, BigDecimal closingBalance) {
}
