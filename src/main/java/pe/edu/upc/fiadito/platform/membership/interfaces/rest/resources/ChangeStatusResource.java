package pe.edu.upc.fiadito.platform.membership.interfaces.rest.resources;

import jakarta.validation.constraints.NotNull;
import pe.edu.upc.fiadito.platform.membership.domain.model.valueobjects.RecordStatus;

public record ChangeStatusResource(@NotNull RecordStatus estado) {
}
