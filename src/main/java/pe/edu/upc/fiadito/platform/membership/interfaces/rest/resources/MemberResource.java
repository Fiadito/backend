package pe.edu.upc.fiadito.platform.membership.interfaces.rest.resources;

import java.math.BigDecimal;

/**
 * Socio con sus condiciones de crédito y su situación de deuda (deuda = capital pendiente).
 */
public record MemberResource(Long id, Long idNegocio, String dni, String nombres, String celular, String direccion, String moneda,
                             BigDecimal limiteCredito, Integer maxCuotas, String estado, String correo, BigDecimal deuda,
                             BigDecimal disponible, boolean conMora) {
}
