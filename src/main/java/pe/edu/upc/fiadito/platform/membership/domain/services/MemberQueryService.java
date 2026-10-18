package pe.edu.upc.fiadito.platform.membership.domain.services;

import pe.edu.upc.fiadito.platform.membership.domain.model.aggregates.Member;

import java.util.List;
import java.util.Optional;

public interface MemberQueryService {
    List<Member> getAllByBusiness(Long businessId);

    Optional<Member> getById(Long businessId, Long memberId);
}
