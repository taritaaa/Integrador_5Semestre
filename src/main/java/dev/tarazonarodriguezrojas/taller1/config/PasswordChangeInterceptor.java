package dev.tarazonarodriguezrojas.taller1.config;

import dev.tarazonarodriguezrojas.taller1.model.entity.User;
import dev.tarazonarodriguezrojas.taller1.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Obliga a cambiar la contraseña temporal antes de usar el sistema.
 * Mientras la cuenta tenga mustChangePassword = true, cualquier página que el usuario
 * abra lo devuelve a /change-password, así no puede saltarse el cambio escribiendo otra URL.
 * El cierre de sesión (POST /logout) lo atiende Spring Security antes, por eso siempre funciona.
 */
@Component
public class PasswordChangeInterceptor implements HandlerInterceptor {

    private final UserRepository repository;

    public PasswordChangeInterceptor(UserRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        // Sin sesión iniciada no se hace nada (login, recuperar contraseña, etc.)
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return true;
        }
        // La propia pantalla de cambio de contraseña debe poder abrirse
        if (request.getRequestURI().equals(request.getContextPath() + "/change-password")) {
            return true;
        }
        boolean mustChange = repository.findByEmailIgnoreCase(auth.getName())
                .map(User::isMustChangePassword)
                .orElse(false);
        if (mustChange) {
            response.sendRedirect(request.getContextPath() + "/change-password");
            return false;
        }
        return true;
    }
}
