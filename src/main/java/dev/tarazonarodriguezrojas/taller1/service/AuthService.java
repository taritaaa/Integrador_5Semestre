package dev.tarazonarodriguezrojas.taller1.service;

import dev.tarazonarodriguezrojas.taller1.model.entity.User;
import dev.tarazonarodriguezrojas.taller1.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
public class AuthService {

    private static final String INVALID_LINK_MESSAGE = "El enlace no es válido o ya expiró. Solicite uno nuevo";

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final String baseUrl;
    private final int tokenMinutes;

    public AuthService(UserRepository repository, PasswordEncoder passwordEncoder, EmailService emailService,
                       @Value("${app.base-url:http://localhost:8080}") String baseUrl,
                       @Value("${app.recovery-token-minutes:30}") int tokenMinutes) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.baseUrl = baseUrl;
        this.tokenMinutes = tokenMinutes;
    }

    // RF-03: solicitar la recuperación de contraseña.
    // Si el correo no existe o la cuenta está inactiva, no se hace nada y no se avisa:
    // así no se revela qué correos están registrados.
    public void requestPasswordRecovery(String email) throws IllegalArgumentException {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Ingrese su correo");
        }
        repository.findByEmailIgnoreCase(email.trim())
                .filter(User::isActive)
                .ifPresent(user -> {
                    String token = UUID.randomUUID().toString();
                    user.setRecoveryToken(token);
                    user.setTokenExpiration(LocalDateTime.now().plusMinutes(tokenMinutes));
                    repository.save(user);
                    emailService.sendPasswordRecovery(user.getEmail(), user.getName(),
                            baseUrl + "/reset-password?token=" + token, tokenMinutes);
                });
    }

    // RF-04: ¿el enlace existe y todavía no expira?
    @Transactional(readOnly = true)
    public boolean isRecoveryTokenValid(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        return repository.findByRecoveryToken(token)
                .filter(u -> u.getTokenExpiration() != null && u.getTokenExpiration().isAfter(LocalDateTime.now()))
                .isPresent();
    }

    // RF-04: establecer la nueva contraseña con el enlace recibido
    public void resetPassword(String token, String newPassword, String confirmPassword)
            throws IllegalArgumentException {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException(INVALID_LINK_MESSAGE);
        }
        User user = repository.findByRecoveryToken(token)
                .filter(u -> u.getTokenExpiration() != null && u.getTokenExpiration().isAfter(LocalDateTime.now()))
                .orElseThrow(() -> new IllegalArgumentException(INVALID_LINK_MESSAGE));

        PasswordPolicy.validate(newPassword);
        if (!newPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("Las contraseñas no coinciden");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        // El enlace solo sirve una vez
        user.setRecoveryToken(null);
        user.setTokenExpiration(null);
        repository.save(user);
    }
}