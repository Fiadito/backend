package pe.edu.upc.fiadito.platform.billing.domain.model.readmodels;

import java.math.BigDecimal;
import java.time.LocalDate;

public record NextPayment(LocalDate date, BigDecimal amount, int count) {
}
