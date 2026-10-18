package pe.edu.upc.fiadito.platform.catalog.domain.model.commands;

import pe.edu.upc.fiadito.platform.membership.domain.model.valueobjects.RecordStatus;

public record ChangeProductStatusCommand(Long businessId, Long productId, RecordStatus status) {
}
