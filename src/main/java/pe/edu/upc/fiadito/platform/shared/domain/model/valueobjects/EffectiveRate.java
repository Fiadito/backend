package pe.edu.upc.fiadito.platform.shared.domain.model.valueobjects;

import java.math.BigDecimal;

/**
 * Value object: tasa efectiva pactada para un periodo (solo se usan tasas efectivas).
 * Fórmula 1 del informe: TED = (1 + TEP)^(1/N) − 1, redondeada a 7 decimales.
 *
 * @param period periodo de la tasa (TEA, TES, TET, TEM o TEQ)
 * @param value  tasa como fracción (0.60 = 60 %)
 */
public record EffectiveRate(RatePeriod period, BigDecimal value) {

    public EffectiveRate {
        if (period == null) throw new IllegalArgumentException("El periodo de la tasa es obligatorio.");
        if (value == null || value.signum() < 0) throw new IllegalArgumentException("La tasa debe ser un número positivo.");
    }

    /** Tasa equivalente para otro número de días: (1 + TEP)^(n2/n1) − 1. */
    public BigDecimal toDays(int targetDays) {
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
}
