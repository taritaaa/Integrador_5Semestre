package dev.tarazonarodriguezrojas.taller1.config;

import dev.tarazonarodriguezrojas.taller1.model.constant.Role;
import dev.tarazonarodriguezrojas.taller1.model.entity.User;
import dev.tarazonarodriguezrojas.taller1.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Crea al iniciar la aplicación los usuarios de prueba, con la contraseña cifrada.
 * La base de datos es en memoria, así que se vuelven a crear en cada arranque.
 * SOLO PARA DESARROLLO: en producción poner app.seed-test-users=false.
 *
 *   admin@taller.test       / Admin12345      (administrador)
 *   mecanico1@taller.test   / Mecanico12345   (mecánico)
 *   mecanico2@taller.test   / Mecanico12345   (mecánico)
 */
@Component
@ConditionalOnProperty(name = "app.seed-test-users", havingValue = "true", matchIfMissing = true)
public class DataInitializer implements CommandLineRunner {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        createIfMissing("Administrador de prueba", "1000000001", "admin@taller.test",
                "3000000001", "Admin12345", Role.ADMIN);
        createIfMissing("Mecánico de prueba 1", "1000000002", "mecanico1@taller.test",
                "3000000002", "Mecanico12345", Role.MECHANIC);
        createIfMissing("Mecánico de prueba 2", "1000000003", "mecanico2@taller.test",
                "3000000003", "Mecanico12345", Role.MECHANIC);
    }

    private void createIfMissing(String name, String document, String email, String phone,
                                 String password, Role role) {
        if (!repository.existsByEmailIgnoreCase(email)) {
            repository.save(new User(name, document, email, phone, passwordEncoder.encode(password), role));
        }
    }
}