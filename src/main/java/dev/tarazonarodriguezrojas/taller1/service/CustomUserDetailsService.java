package dev.tarazonarodriguezrojas.taller1.service;

import dev.tarazonarodriguezrojas.taller1.model.entity.User;
import dev.tarazonarodriguezrojas.taller1.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Le dice a Spring Security cómo buscar un usuario al iniciar sesión (RF-01):
 * por su correo, con su contraseña cifrada y su rol.
 * Una cuenta desactivada no puede iniciar sesión.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository repository;

    public CustomUserDetailsService(UserRepository repository) {
        this.repository = repository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = repository.findByEmailIgnoreCase(email.trim())
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        // Se usa el nombre completo de la clase de Spring porque nuestra entidad también se llama User
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPasswordHash())
                .roles(user.getRole().name())
                .disabled(!user.isActive())
                .build();
    }
}