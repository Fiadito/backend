package pe.edu.upc.fiadito.platform.iam.domain.services;

import pe.edu.upc.fiadito.platform.iam.domain.model.aggregates.Business;
import pe.edu.upc.fiadito.platform.iam.domain.model.aggregates.User;

import java.util.Optional;

public interface IamQueryService {
    Optional<Business> getBusinessById(Long businessId);

    Optional<User> getUserById(Long userId);

    Optional<User> getUserByMemberId(Long memberId);
}
