package pe.edu.upc.fiadito.platform.credit.domain.model.valueobjects;

import java.math.BigDecimal;

/**
 * Reglas del crédito (Tabla 1 del informe y Anexo A del enunciado).
 */
public final class CreditPolicy {

    /** Monto mínimo y máximo del límite de crédito y de cada compra. */
    public static final BigDecimal MIN_AMOUNT = new BigDecimal("400.00");
    public static final BigDecimal MAX_AMOUNT = new BigDecimal("1000.00");
    /** Máximo de cuotas quincenales (4 meses). */
    public static final int MAX_INSTALLMENTS = 8;
    /** Tasa efectiva moratoria mínima (40 %). */
    public static final BigDecimal MIN_LATE_RATE = new BigDecimal("0.40");

    private CreditPolicy() {
    }

    public static boolean isAmountInRange(BigDecimal amount) {
        return amount.compareTo(MIN_AMOUNT) >= 0 && amount.compareTo(MAX_AMOUNT) <= 0;
    }
}
