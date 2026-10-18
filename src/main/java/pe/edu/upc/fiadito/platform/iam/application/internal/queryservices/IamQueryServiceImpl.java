package pe.edu.upc.fiadito.platform.iam.application.internal.queryservices;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.fiadito.platform.iam.domain.model.aggregates.Business;
import pe.edu.upc.fiadito.platform.iam.domain.model.aggregates.User;
import pe.edu.upc.fiadito.platform.iam.domain.services.IamQueryService;
import pe.edu.upc.fiadito.platform.iam.infrastructure.persistence.jpa.repositories.BusinessRepository;
import pe.edu.upc.fiadito.platform.iam.infrastructure.persistence.jpa.repositories.UserRepository;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class IamQueryServiceImpl implements IamQueryService {

    private final BusinessRepository businessRepository;
    private final UserRepository userRepository;

    @Override
    public Optional<Business> getBusinessById(Long businessId) {
        return businessRepository.findById(businessId);
    }

    @Override
    public Optional<User> getUserById(Long userId) {
        return userRepository.findById(userId);
    }

    @Override
    public Optional<User> getUserByMemberId(Long memberId) {
        return userRepository.findByMemberId(memberId);
    }
}
