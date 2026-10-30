package dev.tarazonarodriguezrojas.taller1.service;

import dev.tarazonarodriguezrojas.taller1.model.constant.Role;
import dev.tarazonarodriguezrojas.taller1.model.dto.UserDTO;
import dev.tarazonarodriguezrojas.taller1.model.entity.User;
import dev.tarazonarodriguezrojas.taller1.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.regex.Pattern;

@Service
@Transactional
public class UserService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    // RF-07: listado de usuarios
    @Transactional(readOnly = true)
    public List<UserDTO> findAllUsers() {
        return repository.findAll().stream().map(this::toDTO).toList();
    }

    // RF-07: detalle de un usuario
    @Transactional(readOnly = true)
    public UserDTO findUserById(Integer userId) throws EntityNotFoundException {
        return toDTO(findEntity(userId));
    }

    // RF-06: registrar usuario (mecánico u otro administrador). La contraseña se guarda cifrada.
    public UserDTO createUser(UserDTO request) throws IllegalArgumentException {
        validateCommon(request);

        if (request.document() == null || request.document().isBlank()) {
            throw new IllegalArgumentException("El documento es obligatorio");
        }
        if (request.document().trim().length() > 20) {
            throw new IllegalArgumentException("El documento no puede superar 20 caracteres");
        }
        if (request.email() == null || request.email().isBlank()) {
            throw new IllegalArgumentException("El correo es obligatorio");
        }
        String email = request.email().trim().toLowerCase();
        if (email.length() > 100 || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("El correo no es válido");
        }
        PasswordPolicy.validate(request.password());

        String document = request.document().trim();
        if (repository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("Ya existe un usuario con el correo " + email);
        }
        if (repository.existsByDocument(document)) {
            throw new IllegalArgumentException("Ya existe un usuario con el documento " + document);
        }

        User user = new User(request.name().trim(), document, email, request.phone().trim(),
                passwordEncoder.encode(request.password()), request.role());
        return toDTO(repository.save(user));
    }

    // RF-07: actualizar nombre, teléfono, rol y estado. Desactivar una cuenta es poner active en false.
    public UserDTO updateUser(Integer userId, UserDTO request)
            throws EntityNotFoundException, IllegalArgumentException, IllegalStateException {
        User user = findEntity(userId);
        validateCommon(request);
        if (request.active() == null) {
            throw new IllegalArgumentException("El estado de la cuenta es obligatorio");
        }

        // Siempre debe quedar al menos un administrador activo
        boolean wasActiveAdmin = user.getRole() == Role.ADMIN && user.isActive();
        boolean staysActiveAdmin = request.role() == Role.ADMIN && request.active();
        if (wasActiveAdmin && !staysActiveAdmin && repository.countByRoleAndActiveTrue(Role.ADMIN) <= 1) {
            throw new IllegalStateException(
                    "Debe quedar al menos un administrador activo: no se puede desactivar ni cambiar el rol de este usuario");
        }

        user.setName(request.name().trim());
        user.setPhone(request.phone().trim());
        user.setRole(request.role());
        user.setActive(request.active());
        return toDTO(repository.save(user));
    }

    private User findEntity(Integer userId) {
        return repository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario con id " + userId + " no encontrado"));
    }

    // La contraseña nunca se devuelve
    private UserDTO toDTO(User user) {
        return new UserDTO(user.getId(), user.getName(), user.getDocument(), user.getEmail(),
                user.getPhone(), null, user.getRole(), user.isActive());
    }

    // Validaciones que comparten el registro y la actualización
    private void validateCommon(UserDTO request) {
        if (request.name() == null || request.name().isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
        if (request.name().trim().length() > 100) {
            throw new IllegalArgumentException("El nombre no puede superar 100 caracteres");
        }
        if (request.phone() == null || request.phone().isBlank()) {
            throw new IllegalArgumentException("El teléfono es obligatorio");
        }
        if (request.phone().trim().length() > 20) {
            throw new IllegalArgumentException("El teléfono no puede superar 20 caracteres");
        }
        if (request.role() == null) {
            throw new IllegalArgumentException("El rol es obligatorio");
        }
    }
}