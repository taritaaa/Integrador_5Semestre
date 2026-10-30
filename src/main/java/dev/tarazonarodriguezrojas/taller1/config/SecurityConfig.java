package dev.tarazonarodriguezrojas.taller1.config;

import dev.tarazonarodriguezrojas.taller1.model.constant.Role;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

/**
 * Seguridad de la aplicación (RF-01, RF-02 y RF-05).
 *
 * - Inicio de sesión con correo y contraseña; cierre de sesión por POST /logout.
 * - Las contraseñas se guardan cifradas con BCrypt.
 * - Administrador: acceso total y único que registra/edita modelos y repuestos, elimina registros
 *   y gestiona usuarios.
 * - Mecánico: consulta modelos y repuestos, registra y actualiza vehículos y reparaciones,
 *   y usa las búsquedas. No elimina ni gestiona usuarios.
 *
 * Las reglas se evalúan en orden: la primera que coincide es la que se aplica.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String ADMIN = Role.ADMIN.name();
    private static final String MECHANIC = Role.MECHANIC.name();

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // Páginas públicas: login, recuperación de contraseña y errores
                        .requestMatchers(antMatcher("/login"), antMatcher("/forgot-password"),
                                antMatcher("/reset-password"), antMatcher("/access-denied"),
                                antMatcher("/error")).permitAll()

                        // Solo administrador: usuarios y consola de la base de datos
                        .requestMatchers(antMatcher("/users/**"), antMatcher("/h2-console/**")).hasRole(ADMIN)

                        // Solo administrador: registrar, editar y eliminar modelos
                        .requestMatchers(
                                antMatcher(HttpMethod.GET, "/models/new"),
                                antMatcher(HttpMethod.GET, "/models/*/edit"),
                                antMatcher(HttpMethod.POST, "/models/**"),
                                antMatcher(HttpMethod.PUT, "/models/**"),
                                antMatcher(HttpMethod.DELETE, "/models/**")).hasRole(ADMIN)

                        // Solo administrador: registrar, editar y eliminar repuestos, y subir imágenes
                        .requestMatchers(
                                antMatcher(HttpMethod.GET, "/parts/new"),
                                antMatcher(HttpMethod.GET, "/parts/*/edit"),
                                antMatcher(HttpMethod.POST, "/parts/**"),
                                antMatcher(HttpMethod.PUT, "/parts/**"),
                                antMatcher(HttpMethod.DELETE, "/parts/**")).hasRole(ADMIN)

                        // Solo administrador: eliminar vehículos y reparaciones
                        .requestMatchers(
                                antMatcher(HttpMethod.DELETE, "/vehicles/**"),
                                antMatcher(HttpMethod.DELETE, "/repairs/**")).hasRole(ADMIN)

                        // Todo lo demás: administrador y mecánico
                        .anyRequest().hasAnyRole(ADMIN, MECHANIC))

                // RF-01: inicio de sesión con correo y contraseña
                .formLogin(form -> form
                        .loginPage("/login")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/", true)
                        .failureUrl("/login?error")
                        .permitAll())

                // RF-02: cierre de sesión seguro (POST con token CSRF)
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll())

                // RF-05: página que se muestra cuando el rol no tiene permiso
                .exceptionHandling(ex -> ex.accessDeniedPage("/access-denied"))

                // La consola de H2 trae su propio formulario y se muestra en un marco
                .csrf(csrf -> csrf.ignoringRequestMatchers(antMatcher("/h2-console/**")))
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin));

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}