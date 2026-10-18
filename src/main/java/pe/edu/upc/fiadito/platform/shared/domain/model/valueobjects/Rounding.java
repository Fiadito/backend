package pe.edu.upc.fiadito.platform.shared.domain.model.valueobjects;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Reglas de redondeo del Anexo A: montos a 2 decimales y tasas a 7 decimales.
 */
public final class Rounding {

    public static final int MONEY_SCALE = 2;
    public static final int RATE_SCALE = 7;

    private Rounding() {
    }

    public static BigDecimal money(BigDecimal value) {
        return value.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }

    public static BigDecimal money(double value) {
        return money(BigDecimal.valueOf(value));
    }

    public static BigDecimal rate(double value) {
        return BigDecimal.valueOf(value).setScale(RATE_SCALE, RoundingMode.HALF_UP);
    }
}
