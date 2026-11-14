package pe.edu.upc.fiadito.platform.membership.application.internal.commandservices;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.fiadito.platform.iam.interfaces.acl.IamContextFacade;
import pe.edu.upc.fiadito.platform.membership.application.internal.outboundservices.acl.ExternalCreditService;
import pe.edu.upc.fiadito.platform.membership.domain.model.aggregates.Member;
import pe.edu.upc.fiadito.platform.membership.domain.model.commands.ChangeMemberStatusCommand;
import pe.edu.upc.fiadito.platform.membership.domain.model.commands.CreateMemberCommand;
import pe.edu.upc.fiadito.platform.membership.domain.model.commands.UpdateMemberCommand;
import pe.edu.upc.fiadito.platform.membership.domain.model.valueobjects.RecordStatus;
import pe.edu.upc.fiadito.platform.membership.domain.services.MemberCommandService;
import pe.edu.upc.fiadito.platform.membership.infrastructure.persistence.jpa.repositories.MemberRepository;
import pe.edu.upc.fiadito.platform.shared.domain.exceptions.ConflictException;
import pe.edu.upc.fiadito.platform.shared.domain.exceptions.ResourceNotFoundException;
import pe.edu.upc.fiadito.platform.shared.domain.model.events.OperationPerformedEvent;

/**
 * GuardarSocio y DarDeBajaSocio (módulo 7.4). El socio y su cuenta se guardan en una sola transacción.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class MemberCommandServiceImpl implements MemberCommandService {

    private final MemberRepository repository;
    private final IamContextFacade iam;
    private final ExternalCreditService credit;
    private final ApplicationEventPublisher events;

    @Override
    public Member handle(CreateMemberCommand c, Long actorUserId, String actorName) {
        if (repository.existsByBusinessIdAndDni(c.businessId(), c.dni())) throw new ConflictException("Ya existe un socio con ese DNI.");
        var hasAccess = c.email() != null && !c.email().isBlank();
        if (hasAccess && iam.emailExists(c.email())) throw new ConflictException("Ya existe una cuenta con ese correo.");
        var member = repository.save(new Member(c.businessId(), c.dni(), c.fullName(), c.phone(), c.address(), c.currency(),
                c.creditLimit(), c.maxInstallments()));
        if (hasAccess) iam.createMemberAccount(c.businessId(), member.getId(), member.getFullName(), c.email(), c.password());
        events.publishEvent(new OperationPerformedEvent(c.businessId(), actorUserId, actorName, "REGISTRAR", "SOCIO", member.getId(),
                "Registro del socio " + member.getFullName() + " (DNI " + member.getDni() + ")"));
        return member;
    }

    @Override
    public Member handle(UpdateMemberCommand c, Long actorUserId, String actorName) {
        var member = find(c.businessId(), c.memberId());
        if (repository.existsByBusinessIdAndDniAndIdNot(c.businessId(), c.dni(), c.memberId())) throw new ConflictException("Ya existe un socio con ese DNI.");
        member.update(c.dni(), c.fullName(), c.phone(), c.address(), c.currency(), c.creditLimit(), c.maxInstallments(),
                credit.pendingDebtOf(member.getId()));
        iam.renameMemberAccount(member.getId(), member.getFullName());
        events.publishEvent(new OperationPerformedEvent(c.businessId(), actorUserId, actorName, "EDITAR", "SOCIO", member.getId(),
                "Edición del socio " + member.getFullName()));
        return repository.save(member);
    }

    @Override
    public Member handle(ChangeMemberStatusCommand c, Long actorUserId, String actorName) {
        var member = find(c.businessId(), c.memberId());
        if (c.status() == RecordStatus.INACTIVO) member.deactivate(credit.pendingDebtOf(member.getId()));
        else member.activate();
        events.publishEvent(new OperationPerformedEvent(c.businessId(), actorUserId, actorName, "EDITAR", "SOCIO", member.getId(),
                (c.status() == RecordStatus.INACTIVO ? "Baja" : "Reactivación") + " del socio " + member.getFullName()));
        return repository.save(member);
    }

    private Member find(Long businessId, Long memberId) {
        return repository.findByIdAndBusinessId(memberId, businessId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el socio " + memberId + "."));
    }
}
