package pe.edu.upc.fiadito.platform.membership.domain.services;

import pe.edu.upc.fiadito.platform.membership.domain.model.aggregates.Member;
import pe.edu.upc.fiadito.platform.membership.domain.model.commands.ChangeMemberStatusCommand;
import pe.edu.upc.fiadito.platform.membership.domain.model.commands.CreateMemberCommand;
import pe.edu.upc.fiadito.platform.membership.domain.model.commands.UpdateMemberCommand;

public interface MemberCommandService {
    Member handle(CreateMemberCommand command, Long actorUserId, String actorName);

    Member handle(UpdateMemberCommand command, Long actorUserId, String actorName);

    Member handle(ChangeMemberStatusCommand command, Long actorUserId, String actorName);
}
