package pe.edu.upc.fiadito.platform.iam.interfaces.rest.resources;

import pe.edu.upc.fiadito.platform.iam.domain.model.aggregates.Business;

import java.time.LocalDateTime;

public record BusinessResource(Long id, String ruc, String nombreComercial, String giro, String direccion, LocalDateTime fechaRegistro) {
    public static BusinessResource from(Business b) {
        return new BusinessResource(b.getId(), b.getRuc(), b.getName(), b.getLine(), b.getAddress(), b.getRegisteredAt());
    }
}
