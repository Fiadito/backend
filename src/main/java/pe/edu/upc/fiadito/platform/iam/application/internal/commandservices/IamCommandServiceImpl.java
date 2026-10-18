package pe.edu.upc.fiadito.platform.iam.application.internal.commandservices;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.fiadito.platform.iam.application.internal.security.JwtService;
import pe.edu.upc.fiadito.platform.iam.domain.model.aggregates.Business;
import pe.edu.upc.fiadito.platform.iam.domain.model.aggregates.User;
import pe.edu.upc.fiadito.platform.iam.domain.model.commands.RegisterBusinessCommand;
import pe.edu.upc.fiadito.platform.iam.domain.model.commands.SignInCommand;
import pe.edu.upc.fiadito.platform.iam.domain.model.commands.SignOutCommand;
import pe.edu.upc.fiadito.platform.iam.domain.model.valueobjects.AuthenticatedUser;
import pe.edu.upc.fiadito.platform.iam.domain.model.valueobjects.Role;
import pe.edu.upc.fiadito.platform.iam.domain.services.IamCommandService;
import pe.edu.upc.fiadito.platform.iam.infrastructure.persistence.jpa.repositories.BusinessRepository;
import pe.edu.upc.fiadito.platform.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import pe.edu.upc.fiadito.platform.shared.application.time.SystemDateProvider;
import pe.edu.upc.fiadito.platform.shared.domain.exceptions.BusinessRuleException;
import pe.edu.upc.fiadito.platform.shared.domain.exceptions.ConflictException;
import pe.edu.upc.fiadito.platform.shared.domain.model.events.OperationPerformedEvent;

/**
 * Registro del negocio, inicio y cierre de sesión (módulos 7.1 y 7.2).
 */
@Service
@Transactional
@RequiredArgsConstructor
public class IamCommandServiceImpl implements IamCommandService {

    private static final String EMAIL_PATTERN = "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$";

    private final BusinessRepository businessRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final SystemDateProvider dates;
    private final ApplicationEventPublisher events;

    @Override
    public Business handle(RegisterBusinessCommand c) {
        var email = c.email() == null ? "" : c.email().trim().toLowerCase();
        if (c.responsible() == null || c.responsible().trim().length() < 3) throw new BusinessRuleException("Ingresa el nombre del responsable.");
        if (!email.matches(EMAIL_PATTERN)) throw new BusinessRuleException("Ingresa un correo electrónico válido.");
        if (c.password() == null || c.password().length() < 8 || !c.password().equals(c.confirmation()))
            throw new BusinessRuleException("La contraseña debe tener 8 caracteres y coincidir.");
        if (userRepository.existsByEmail(email)) throw new ConflictException("Ya existe una cuenta con ese correo.");
        if (businessRepository.existsByRuc(c.ruc())) throw new ConflictException("Ya existe un negocio registrado con ese RUC.");

        var business = businessRepository.save(new Business(c.ruc(), c.name(), c.line(), c.address(), dates.now()));
        var user = userRepository.save(new User(business.getId(), null, c.responsible().trim(), email,
                passwordEncoder.encode(c.password()), Role.NEGOCIO));
        events.publishEvent(new OperationPerformedEvent(business.getId(), user.getId(), user.getName(), "REGISTRAR", "NEGOCIO",
                business.getId(), "Registro del negocio " + business.getName() + " (RUC " + business.getRuc() + ")"));
        return business;
    }

    @Override
    @Transactional(noRollbackFor = BadCredentialsException.class)
    public AuthenticatedUser handle(SignInCommand c) {
        var email = c.email() == null ? "" : c.email().trim().toLowerCase();
        var user = userRepository.findByEmail(email).orElse(null);
        if (user == null || c.password() == null || !passwordEncoder.matches(c.password(), user.getPasswordHash())) {
            events.publishEvent(new OperationPerformedEvent(user == null ? null : user.getBusinessId(), null, email,
                    "LOGIN_FALLIDO", "USUARIO", null, "Intento fallido con " + email));
            throw new BadCredentialsException("Correo o contraseña incorrectos.");
        }
        var business = businessRepository.findById(user.getBusinessId()).orElseThrow();
        events.publishEvent(new OperationPerformedEvent(user.getBusinessId(), user.getId(), user.getName(), "LOGIN", "USUARIO",
                user.getId(), "Inicio de sesión (" + user.getRole() + ")"));
        return new AuthenticatedUser(user, business, jwtService.generateToken(user));
    }

    @Override
    public void handle(SignOutCommand c) {
        userRepository.findById(c.userId()).ifPresent(user -> events.publishEvent(new OperationPerformedEvent(
                user.getBusinessId(), user.getId(), user.getName(), "LOGOUT", "USUARIO", user.getId(), "Cierre de sesión")));
    }
}
