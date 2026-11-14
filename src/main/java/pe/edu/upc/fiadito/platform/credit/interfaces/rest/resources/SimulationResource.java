package pe.edu.upc.fiadito.platform.credit.interfaces.rest.resources;

import pe.edu.upc.fiadito.platform.credit.domain.model.valueobjects.SaleSimulation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Resultado de POST /ventas/simulacion: errores de validación (si hay), montos, tasa, interés y plan de pagos. */
public record SimulationResource(List<String> errores, BigDecimal precioLista, BigDecimal monto, String modalidad, Integer nCuotas,
                                 LocalDate fechaCorte, LocalDate fechaPago, String periodoTasa, BigDecimal valorTasa,
                                 BigDecimal tasaQuincenal, BigDecimal teaEquivalente, BigDecimal cuota, BigDecimal interesTotal,
                                 BigDecimal totalAPagar, BigDecimal deudaSocio, BigDecimal creditoDisponible,
                                 List<ScheduleRowResource> cronograma) {
    public static SimulationResource from(SaleSimulation s) {
        return new SimulationResource(s.errors(), s.listPrice(), s.amount(), s.paymentMode() == null ? null : s.paymentMode().name(),
                s.installmentCount(), s.cutoffDate(), s.paymentDate(), s.ratePeriod() == null ? null : s.ratePeriod().name(),
                s.rateValue(), s.periodRate(), s.annualRate(), s.installmentAmount(), s.interestTotal(), s.totalToPay(), s.memberDebt(),
                s.availableCredit(), s.schedule().stream().map(ScheduleRowResource::from).toList());
    }
}
