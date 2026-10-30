package dev.tarazonarodriguezrojas.taller1.repository;

import dev.tarazonarodriguezrojas.taller1.model.constant.Role;
import dev.tarazonarodriguezrojas.taller1.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    // Mecánicos activos, para elegir quién atiende una reparación
    List<User> findByRoleAndActiveTrue(Role role);

    // RF-01 y RF-03: buscar un usuario por su correo
    Optional<User> findByEmailIgnoreCase(String email);

    // RF-04: buscar el usuario dueño de un token de recuperación
    Optional<User> findByRecoveryToken(String recoveryToken);

    // RF-06: ¿ya existe ese correo o ese documento?
    boolean existsByEmailIgnoreCase(String email);

    boolean existsByDocument(String document);

    // Regla: siempre debe quedar al menos un administrador activo
    long countByRoleAndActiveTrue(Role role);
}