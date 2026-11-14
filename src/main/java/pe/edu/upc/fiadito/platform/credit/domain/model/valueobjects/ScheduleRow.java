package pe.edu.upc.fiadito.platform.credit.domain.model.valueobjects;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Fila del plan de pagos: n_cuota, fecha_vencimiento, saldo_inicial, interés, amortización, cuota y saldo_final.
 */
public record ScheduleRow(int number, LocalDate dueDate, BigDecimal openingBalance, BigDecimal interest, BigDecimal amortization,
                          BigDecimal amount, BigDecimal closingBalance) {
}
