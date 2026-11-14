package pe.edu.upc.fiadito.platform.membership.interfaces.acl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.fiadito.platform.membership.domain.model.aggregates.Member;
import pe.edu.upc.fiadito.platform.membership.infrastructure.persistence.jpa.repositories.MemberRepository;

import java.util.List;
import java.util.Optional;

/**
 * Fachada del contexto Membership para Credit y Billing (condiciones de crédito del socio).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MembershipContextFacade {

    private final MemberRepository repository;

    public Optional<Member> findMember(Long businessId, Long memberId) {
        return repository.findByIdAndBusinessId(memberId, businessId);
    }

    public Optional<Member> findMemberById(Long memberId) {
        return repository.findById(memberId);
    }

    public List<Member> membersOf(Long businessId) {
        return repository.findAllByBusinessIdOrderByFullNameAsc(businessId);
    }

    /** Bloquea al socio dentro de la transacción de la venta (si no existe, la simulación lo reporta). */
    @Transactional
    public void lockMember(Long businessId, Long memberId) {
        repository.findForUpdate(memberId, businessId);
    }
}
