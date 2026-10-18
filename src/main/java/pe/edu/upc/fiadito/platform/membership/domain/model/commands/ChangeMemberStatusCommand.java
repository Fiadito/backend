package pe.edu.upc.fiadito.platform.membership.domain.model.commands;

import pe.edu.upc.fiadito.platform.membership.domain.model.valueobjects.RecordStatus;

public record ChangeMemberStatusCommand(Long businessId, Long memberId, RecordStatus status) {
}
