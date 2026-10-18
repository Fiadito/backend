package pe.edu.upc.fiadito.platform.catalog.domain.model.valueobjects;

import pe.edu.upc.fiadito.platform.credit.domain.model.valueobjects.PaymentMode;

/** Modalidad de venta permitida para un producto. */
public enum SaleMode {
    PAGO_UNICO, CUOTAS, AMBAS;

    public boolean allows(PaymentMode mode) {
        return this == AMBAS || name().equals(mode.name());
    }
}
