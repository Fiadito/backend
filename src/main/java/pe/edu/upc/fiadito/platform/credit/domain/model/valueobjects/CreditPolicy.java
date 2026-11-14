package pe.edu.upc.fiadito.platform.credit.domain.model.valueobjects;

import pe.edu.upc.fiadito.platform.shared.domain.model.valueobjects.EffectiveRate;
import pe.edu.upc.fiadito.platform.shared.domain.model.valueobjects.RatePeriod;

import java.math.BigDecimal;

/**
 * Reglas del crédito.
 * - Límite de crédito y cada compra entre S/ 400.00 y S/ 1,000.00 (los combos de S/ 100 y 200 se compran en cantidad).
 * - Tasa efectiva de la venta (interés compensatorio del plan): su TEA equivalente entre 15 % y 27 %.
 * - Tasa efectiva moratoria: se coloca al pagar algo vencido; TEA equivalente mínima 40 %, 60 % por defecto.
 */
public final class CreditPolicy {

    /** Monto mínimo y máximo del límite de crédito y de cada compra. */
    public static final BigDecimal MIN_AMOUNT = new BigDecimal("400.00");
    public static final BigDecimal MAX_AMOUNT = new BigDecimal("1000.00");
    /** Máximo de cuotas quincenales (4 meses). */
    public static final int MAX_INSTALLMENTS = 8;
    /** Rango de la tasa efectiva de la venta (TEA equivalente). */
    public static final BigDecimal MIN_SALE_RATE = new BigDecimal("0.15");
    public static final BigDecimal MAX_SALE_RATE = new BigDecimal("0.27");
    /** Tasa efectiva moratoria mínima (TEA equivalente) y la sugerida por defecto. */
    public static final BigDecimal MIN_LATE_RATE = new BigDecimal("0.40");
    public static final EffectiveRate DEFAULT_LATE_RATE = new EffectiveRate(RatePeriod.TEA, new BigDecimal("0.60"));

    private CreditPolicy() {
    }

    public static boolean isAmountInRange(BigDecimal amount) {
        return amount.compareTo(MIN_AMOUNT) >= 0 && amount.compareTo(MAX_AMOUNT) <= 0;
    }

    /** @return mensaje de error, o null si la tasa de la venta es válida. */
    public static String saleRateError(RatePeriod period, BigDecimal value) {
        if (period == null) return "Selecciona el periodo de la tasa efectiva de la venta.";
        if (value == null || value.signum() <= 0) return "Ingresa la tasa efectiva de la venta.";
        if (!new EffectiveRate(period, value).annualBetween(MIN_SALE_RATE, MAX_SALE_RATE))
            return "La tasa efectiva de la venta debe equivaler a una TEA entre 15 % y 27 %.";
        return null;
    }

    /** @return mensaje de error, o null si la tasa moratoria es válida. */
    public static String lateRateError(EffectiveRate rate) {
        if (!rate.annualBetween(MIN_LATE_RATE, null)) return "La tasa moratoria debe equivaler a una TEA de al menos 40 %.";
        return null;
    }
}
