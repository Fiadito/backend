package pe.edu.upc.fiadito.platform.shared.domain.model.valueobjects;

/**
 * Periodos en que se pacta una tasa efectiva (año comercial de 360 días).
 */
public enum RatePeriod {
    TEA(360), TES(180), TET(90), TEM(30), TEQ(15);

    private final int days;

    RatePeriod(int days) {
        this.days = days;
    }

    /** @return N: días del periodo de la tasa. */
    public int days() {
        return days;
    }
}
