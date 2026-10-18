package pe.edu.upc.fiadito.platform.billing.interfaces.rest.resources;

import java.time.LocalDate;

public record CutoffOptionResource(LocalDate fechaCorte, LocalDate fechaPago, boolean actual) {
}
