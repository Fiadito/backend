package pe.edu.upc.fiadito.platform.credit.interfaces.rest.resources;

import pe.edu.upc.fiadito.platform.credit.domain.model.valueobjects.ScheduleRow;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ScheduleRowResource(Integer numero, LocalDate fechaVencimiento, BigDecimal saldoInicial, BigDecimal interes,
                                  BigDecimal amortizacion, BigDecimal montoCuota, BigDecimal saldoFinal) {
    public static ScheduleRowResource from(ScheduleRow r) {
        return new ScheduleRowResource(r.number(), r.dueDate(), r.openingBalance(), r.interest(), r.amortization(), r.amount(),
                r.closingBalance());
    }
}
