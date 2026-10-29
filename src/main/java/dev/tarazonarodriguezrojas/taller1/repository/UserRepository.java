package dev.tarazonarodriguezrojas.taller1.repository;

import dev.tarazonarodriguezrojas.taller1.model.constant.Role;
import dev.tarazonarodriguezrojas.taller1.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    // Mecánicos activos, para elegir quién atiende una reparación
    List<User> findByRoleAndActiveTrue(Role role);
}