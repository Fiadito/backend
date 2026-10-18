package pe.edu.upc.fiadito.platform.iam.interfaces.acl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.fiadito.platform.iam.domain.model.aggregates.User;
import pe.edu.upc.fiadito.platform.iam.domain.model.valueobjects.Role;
import pe.edu.upc.fiadito.platform.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import pe.edu.upc.fiadito.platform.shared.domain.exceptions.BusinessRuleException;
import pe.edu.upc.fiadito.platform.shared.domain.exceptions.ConflictException;

/**
 * Fachada del contexto IAM para otros contextos (Membership crea la cuenta SOCIO al registrar un socio).
 */
@Service
@RequiredArgsConstructor
public class IamContextFacade {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email.trim().toLowerCase());
    }

    /** GuardarUsuario(nombre, correo, clave, rol ← "SOCIO", idNegocio, idSocio). */
    @Transactional
    public Long createMemberAccount(Long businessId, Long memberId, String name, String email, String password) {
        var normalized = email.trim().toLowerCase();
        if (!normalized.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) throw new BusinessRuleException("Ingresa un correo electrónico válido.");
        if (password == null || password.length() < 8) throw new BusinessRuleException("La contraseña inicial debe tener al menos 8 caracteres.");
        if (userRepository.existsByEmail(normalized)) throw new ConflictException("Ya existe una cuenta con ese correo.");
        return userRepository.save(new User(businessId, memberId, name, normalized, passwordEncoder.encode(password), Role.SOCIO)).getId();
    }

    @Transactional(readOnly = true)
    public String emailOfMember(Long memberId) {
        return userRepository.findByMemberId(memberId).map(User::getEmail).orElse("");
    }

    /** Mantiene el nombre visible de la cuenta del socio cuando se edita el socio. */
    @Transactional
    public void renameMemberAccount(Long memberId, String name) {
        userRepository.findByMemberId(memberId).ifPresent(user -> user.rename(name));
    }
}
