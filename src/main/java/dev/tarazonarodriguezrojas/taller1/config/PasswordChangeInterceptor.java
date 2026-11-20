
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
 * Interceptor encargado de verificar si un usuario debe cambiar
 * su contraseña temporal antes de acceder a las funcionalidades
 * del sistema de gestión del taller automotriz.
 *
 *
 * Su funcionamiento consiste en comprobar el atributo
 * mustChangePassword del usuario autenticado:
 *
 * - Si el atributo es true, redirige al usuario a la página
 *   /change-password para que actualice su contraseña.
 *
 * - Si el atributo es false, permite el acceso normal
 *   a las funcionalidades del sistema.
 *
 * - Si el usuario no ha iniciado sesión, permite continuar
 *   la solicitud sin realizar la validación.
 *
 * También permite acceder a la página de cambio de contraseña
 * para evitar redirecciones infinitas.
 *
 * El cierre de sesión mediante POST /logout es procesado
 * directamente por Spring Security.
 *
 * @see HandlerInterceptor
 * @see UserRepository
 * @see User
 */
@Component
public class PasswordChangeInterceptor implements HandlerInterceptor {

    /**
     * Repositorio utilizado para consultar la información
     * de los usuarios almacenados en la base de datos.
     *
     * Permite verificar si el usuario autenticado tiene
     * pendiente el cambio de su contraseña temporal.
     */
    private final UserRepository repository;

    /**
     * Constructor del interceptor.
     *
     * Spring utiliza la inyección de dependencias para
     * proporcionar una instancia de UserRepository.
     *
     * @param repository Repositorio encargado de consultar
     *                   los usuarios en la base de datos.
     */
    public PasswordChangeInterceptor(UserRepository repository) {
        this.repository = repository;
    }

    /**
     * Intercepta las solicitudes HTTP antes de que sean
     * procesadas por el controlador correspondiente.
     *
     * Verifica si existe un usuario autenticado y consulta
     * si debe cambiar su contraseña temporal.
     *
     * Cuando el cambio es obligatorio, redirige al usuario
     * a /change-password y detiene el procesamiento
     * de la solicitud original.
     *
     * @param request Solicitud HTTP realizada por el usuario.
     * @param response Respuesta HTTP que enviará el servidor.
     * @param handler Controlador o recurso encargado de
     *                procesar la solicitud.
     *
     * @return true si la solicitud puede continuar;
     *         false si el usuario debe ser redirigido.
     *
     * @throws Exception Si ocurre un error durante
     *                   el procesamiento de la solicitud.
     */
    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler) throws Exception {

        // Obtiene la información de autenticación del usuario
        // desde el contexto de seguridad de Spring Security.
        Authentication auth = SecurityContextHolder
                .getContext()
                .getAuthentication();

        // Verifica si el usuario no tiene una sesión autenticada.
        //
        // Si no existe autenticación, no está autenticado o
        // corresponde a un usuario anónimo, permite continuar.
        //
        // Esto evita que el interceptor interfiera con el
        // inicio de sesión y otros accesos no autenticados.
        if (auth == null
                || !auth.isAuthenticated()
                || auth instanceof AnonymousAuthenticationToken) {

            return true;
        }

        // Comprueba si la solicitud corresponde a la página
        // donde el usuario debe cambiar su contraseña.
        //
        // Si se encuentra en /change-password, permite
        // continuar para evitar un ciclo de redirecciones.
        if (request.getRequestURI().equals(
                request.getContextPath() + "/change-password")) {

            return true;
        }

        // Busca al usuario autenticado en la base de datos
        // utilizando su correo electrónico.
        //
        // findByEmailIgnoreCase realiza la búsqueda sin
        // distinguir entre mayúsculas y minúsculas.
        //
        // Si encuentra al usuario, obtiene el valor del
        // atributo mustChangePassword.
        //
        // Si no lo encuentra, establece false por defecto.
        boolean mustChange = repository
                .findByEmailIgnoreCase(auth.getName())
                .map(User::isMustChangePassword)
                .orElse(false);

        // Verifica si el usuario tiene pendiente
        // el cambio obligatorio de contraseña.
        if (mustChange) {

            // Redirige al usuario a la página donde
            // debe establecer una nueva contraseña.
            response.sendRedirect(
                    request.getContextPath() + "/change-password");

            // Detiene el procesamiento de la solicitud
            // para impedir el acceso a la página solicitada.
            return false;
        }

        // Si el usuario no necesita cambiar su contraseña,
        // permite que la solicitud continúe normalmente.
        return true;
    }
}
