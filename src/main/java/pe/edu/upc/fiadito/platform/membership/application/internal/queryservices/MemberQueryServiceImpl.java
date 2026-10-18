package pe.edu.upc.fiadito.platform.membership.application.internal.queryservices;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.fiadito.platform.membership.domain.model.aggregates.Member;
import pe.edu.upc.fiadito.platform.membership.domain.services.MemberQueryService;
import pe.edu.upc.fiadito.platform.membership.infrastructure.persistence.jpa.repositories.MemberRepository;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberQueryServiceImpl implements MemberQueryService {

    private final MemberRepository repository;

    @Override
    public List<Member> getAllByBusiness(Long businessId) {
        return repository.findAllByBusinessIdOrderByFullNameAsc(businessId);
    }

    @Override
    public Optional<Member> getById(Long businessId, Long memberId) {
        return repository.findByIdAndBusinessId(memberId, businessId);
    }
}
