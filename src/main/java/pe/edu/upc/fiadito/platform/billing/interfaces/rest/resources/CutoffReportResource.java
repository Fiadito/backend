package pe.edu.upc.fiadito.platform.billing.interfaces.rest.resources;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record CutoffReportResource(LocalDate fechaCorte, LocalDate fechaPago, boolean preliminar, BigDecimal total, Integer cantidadItems,
                                   List<Group> grupos) {
    public record Group(Long idSocio, String dni, String nombres, String moneda, BigDecimal total, List<ObligationResource> items) {
    }
}
