package dev.tarazonarodriguezrojas.taller1.controller;

import dev.tarazonarodriguezrojas.taller1.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Pantallas de acceso: inicio de sesión (RF-01), recuperación de contraseña (RF-03),
 * restablecimiento (RF-04) y acceso denegado (RF-05).
 * El envío del formulario de login (POST /login) y el cierre de sesión (POST /logout,
 * RF-02) los maneja Spring Security, configurado en SecurityConfig.
 */
@Controller
public class AuthController {

    private final AuthService service;

    public AuthController(AuthService service) {
        this.service = service;
    }

    // RF-01: formulario de inicio de sesión
    @GetMapping("/login")
    public String getLoginTemplate(Authentication authentication) {
        // Si ya inició sesión, se envía al inicio
        if (authentication != null) {
            return "redirect:/";
        }
        return "auth/login";
    }

    // RF-03: formulario para pedir el enlace de recuperación
    @GetMapping("/forgot-password")
    public String getForgotPasswordTemplate() {
        return "auth/forgot-password";
    }

    // RF-03: pedir el enlace. La respuesta es siempre la misma, exista o no el correo.
    @PostMapping("/forgot-password")
    public String requestRecovery(@RequestParam("email") String email, Model model) {
        try {
            service.requestPasswordRecovery(email);
            model.addAttribute("infoMessage",
                    "Si el correo está registrado, recibirá un enlace para restablecer su contraseña");
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
        }
        return "auth/forgot-password";
    }

    // RF-04: formulario de nueva contraseña (el enlace trae el token)
    @GetMapping("/reset-password")
    public String getResetPasswordTemplate(@RequestParam(name = "token", required = false) String token,
                                           Model model) {
        model.addAttribute("token", token);
        model.addAttribute("tokenValid", service.isRecoveryTokenValid(token));
        return "auth/reset-password";
    }

    // RF-04: guardar la nueva contraseña
    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam("token") String token,
                                @RequestParam("newPassword") String newPassword,
                                @RequestParam("confirmPassword") String confirmPassword,
                                Model model, RedirectAttributes redirectAttributes) {
        try {
            service.resetPassword(token, newPassword, confirmPassword);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Contraseña actualizada. Ya puede iniciar sesión");
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            model.addAttribute("token", token);
            model.addAttribute("tokenValid", service.isRecoveryTokenValid(token));
            model.addAttribute("errorMessage", e.getMessage());
            return "auth/reset-password";
        }
    }

    // RF-05: página que se muestra cuando el rol no tiene permiso
    @GetMapping("/access-denied")
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String getAccessDeniedTemplate() {
        return "auth/access-denied";
    }
}