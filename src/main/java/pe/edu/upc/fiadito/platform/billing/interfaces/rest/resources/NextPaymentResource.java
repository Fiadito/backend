package pe.edu.upc.fiadito.platform.billing.interfaces.rest.resources;

import java.math.BigDecimal;
import java.time.LocalDate;

public record NextPaymentResource(LocalDate fecha, BigDecimal monto, Integer cantidad) {
}
