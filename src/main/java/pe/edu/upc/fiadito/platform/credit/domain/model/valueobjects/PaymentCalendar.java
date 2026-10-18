package pe.edu.upc.fiadito.platform.credit.domain.model.valueobjects;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio de dominio: fechas de corte y de pago (reglas fijas del sistema).
 * Cortes: días 10 y 25 hasta las 23:59. Pagos: días 15 y 30 (o el último día del mes).
 * Una compra hasta el corte se paga en la fecha de pago de ese ciclo; después del corte pasa
 * a la siguiente (módulo 7.6: PrimeraFechaPago y SiguienteFechaPago).
 */
public final class PaymentCalendar {

    private PaymentCalendar() {
    }

    /** Segunda fecha de pago del mes: día 30 o el último día (28/29 en febrero). */
    public static LocalDate endOfMonthPayment(int year, int month) {
        var ym = YearMonth.of(year, month);
        return ym.atDay(Math.min(30, ym.lengthOfMonth()));
    }

    private static LocalDate fifteenthOfNextMonth(LocalDate date) {
        return date.withDayOfMonth(1).plusMonths(1).withDayOfMonth(15);
    }

    /** PrimeraFechaPago(fechaHoraCompra). */
    public static LocalDate firstPaymentDate(LocalDateTime purchase) {
        return firstPaymentDate(purchase.toLocalDate());
    }

    public static LocalDate firstPaymentDate(LocalDate date) {
        if (date.getDayOfMonth() <= 10) return date.withDayOfMonth(15);
        if (date.getDayOfMonth() <= 25) return endOfMonthPayment(date.getYear(), date.getMonthValue());
        return fifteenthOfNextMonth(date);
    }

    /** SiguienteFechaPago(fecha). */
    public static LocalDate nextPaymentDate(LocalDate paymentDate) {
        if (paymentDate.getDayOfMonth() == 15) return endOfMonthPayment(paymentDate.getYear(), paymentDate.getMonthValue());
        return fifteenthOfNextMonth(paymentDate);
    }

    /** Fecha de corte que cierra el ciclo de una compra (día 10 o 25). */
    public static LocalDate cutoffOf(LocalDateTime purchase) {
        var date = purchase.toLocalDate();
        if (date.getDayOfMonth() <= 10) return date.withDayOfMonth(10);
        if (date.getDayOfMonth() <= 25) return date.withDayOfMonth(25);
        return date.withDayOfMonth(1).plusMonths(1).withDayOfMonth(10);
    }

    /** FechaPagoDelCorte: 10 → 15, 25 → 30 (o último día). */
    public static LocalDate paymentDateOfCutoff(LocalDate cutoff) {
        if (cutoff.getDayOfMonth() == 10) return cutoff.withDayOfMonth(15);
        if (cutoff.getDayOfMonth() == 25) return endOfMonthPayment(cutoff.getYear(), cutoff.getMonthValue());
        throw new IllegalArgumentException("La fecha de corte debe ser el día 10 o 25.");
    }

    /** Corte cuya fecha de pago aún no pasa (valor por defecto del reporte). */
    public static LocalDate currentCutoff(LocalDate today) {
        if (today.getDayOfMonth() <= 15) return today.withDayOfMonth(10);
        if (today.getDayOfMonth() <= endOfMonthPayment(today.getYear(), today.getMonthValue()).getDayOfMonth()) return today.withDayOfMonth(25);
        return today.withDayOfMonth(1).plusMonths(1).withDayOfMonth(10);
    }

    /** Cortes alrededor de una fecha, del más reciente al más antiguo. */
    public static List<LocalDate> cutoffsAround(LocalDate reference, int monthsBack, int monthsAhead) {
        var first = reference.withDayOfMonth(1);
        var result = new ArrayList<LocalDate>();
        for (int offset = monthsAhead; offset >= -monthsBack; offset--) {
            var month = first.plusMonths(offset);
            result.add(month.withDayOfMonth(25));
            result.add(month.withDayOfMonth(10));
        }
        return result;
    }
}
