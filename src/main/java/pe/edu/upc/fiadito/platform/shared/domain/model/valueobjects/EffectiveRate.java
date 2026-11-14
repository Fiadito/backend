package pe.edu.upc.fiadito.platform.shared.domain.model.valueobjects;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Value object: tasa efectiva pactada para un periodo (solo se usan tasas efectivas).
 * Conversión entre periodos: TEP2 = (1 + TEP1)^(n2/n1) − 1, con 7 decimales en porcentaje.
 * Los rangos (15 %–27 % de la venta, mínimo 40 % de la mora) se comparan sobre la TEA equivalente,
 * así una TEQ o una TEM se validan igual que una TEA.
 *
 * @param period periodo de la tasa (TEA, TES, TET, TEM o TEQ)
 * @param value  tasa como fracción (0.20 = 20 %)
 */
public record EffectiveRate(RatePeriod period, BigDecimal value) {

    public EffectiveRate {
        if (period == null) throw new IllegalArgumentException("El periodo de la tasa es obligatorio.");
        if (value == null || value.signum() < 0) throw new IllegalArgumentException("La tasa debe ser un número positivo.");
    }

    /** Tasa equivalente para otro número de días: (1 + TEP)^(n2/n1) − 1. */
    public BigDecimal toDays(int targetDays) {
        if (targetDays == period.days()) return value.setScale(Rounding.RATE_SCALE, RoundingMode.HALF_UP);
        return Rounding.rate(Math.pow(1 + value.doubleValue(), (double) targetDays / period.days()) - 1);
    }

    /** TED: tasa efectiva diaria. */
    public BigDecimal toDaily() {
        return toDays(1);
    }

    /** Tasa equivalente en otro periodo. */
    public BigDecimal toPeriod(RatePeriod target) {
        return toDays(target.days());
    }

    /** TEA equivalente. */
    public BigDecimal toAnnual() {
        return toPeriod(RatePeriod.TEA);
    }

    /** ¿La TEA equivalente está entre min y max? Se compara con 6 decimales para no rechazar por redondeo. */
    public boolean annualBetween(BigDecimal min, BigDecimal max) {
        var annual = toAnnual().setScale(6, RoundingMode.HALF_UP);
        return (min == null || annual.compareTo(min) >= 0) && (max == null || annual.compareTo(max) <= 0);
    }
}
