package pe.edu.upc.fiadito.platform.credit.interfaces.rest.resources;

import java.math.BigDecimal;
import java.time.LocalDate;

public record InstallmentResource(Long id, Long idVenta, Long idSocio, Integer numero, Integer nCuotas, LocalDate fechaVencimiento,
                                  BigDecimal saldoInicial, BigDecimal interes, BigDecimal amortizacion, BigDecimal montoCuota,
                                  BigDecimal saldoFinal, String estado) {
}
