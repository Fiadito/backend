package pe.edu.upc.fiadito.platform.credit.domain.model.valueobjects;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Resultado de simular una venta (POST /ventas/simulacion): validaciones, monto, fecha de pago y plan de pagos.
 */
public record SaleSimulation(List<String> errors, BigDecimal listPrice, BigDecimal amount, PaymentMode paymentMode,
                             int installmentCount, LocalDate cutoffDate, LocalDate paymentDate, BigDecimal installmentAmount,
                             BigDecimal totalToPay, List<ScheduleRow> schedule, BigDecimal memberDebt, BigDecimal availableCredit) {
    public boolean isValid() {
        return errors.isEmpty();
    }
}
