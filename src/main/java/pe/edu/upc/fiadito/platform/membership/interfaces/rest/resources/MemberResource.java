package pe.edu.upc.fiadito.platform.membership.interfaces.rest.resources;

import java.math.BigDecimal;

/**
 * Socio con sus condiciones de crédito, la conversión de la tasa moratoria a TED y su situación de deuda.
 */
public record MemberResource(Long id, Long idNegocio, String dni, String nombres, String celular, String direccion, String moneda,
                             String periodoTasaMora, BigDecimal valorTasaMora, BigDecimal tedMora, BigDecimal limiteCredito,
                             Integer maxCuotas, String estado, String correo, BigDecimal deuda, BigDecimal disponible, boolean conMora) {
}
