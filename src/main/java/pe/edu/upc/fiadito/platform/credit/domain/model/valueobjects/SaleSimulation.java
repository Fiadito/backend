package pe.edu.upc.fiadito.platform.credit.domain.model.valueobjects;

import pe.edu.upc.fiadito.platform.shared.domain.model.valueobjects.RatePeriod;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Resultado de simular una venta (POST /ventas/simulacion): validaciones, monto, tasa de la venta, fecha de pago,
 * interés y plan de pagos.
 *
 * @param periodRate TEQ: tasa efectiva quincenal de la venta (la que se usa en cada cuota)
 * @param annualRate TEA equivalente de la tasa de la venta
 */
public record SaleSimulation(List<String> errors, BigDecimal listPrice, BigDecimal amount, PaymentMode paymentMode,
                             int installmentCount, LocalDate cutoffDate, LocalDate paymentDate, RatePeriod ratePeriod,
                             BigDecimal rateValue, BigDecimal periodRate, BigDecimal annualRate, BigDecimal installmentAmount,
                             BigDecimal interestTotal, BigDecimal totalToPay, List<ScheduleRow> schedule, BigDecimal memberDebt,
                             BigDecimal availableCredit) {
    public boolean isValid() {
        return errors.isEmpty();
    }
}
