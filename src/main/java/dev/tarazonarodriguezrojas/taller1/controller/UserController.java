package dev.tarazonarodriguezrojas.taller1.controller;

import dev.tarazonarodriguezrojas.taller1.model.constant.Role;
import dev.tarazonarodriguezrojas.taller1.model.dto.UserDTO;
import dev.tarazonarodriguezrojas.taller1.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Pantallas de usuarios (RF-06 y RF-07). Solo para el administrador (ver SecurityConfig).
 * Las cuentas se pueden desactivar (conserva el historial) o eliminar (solo si no tienen
 * reparaciones asociadas). Al registrar un usuario el sistema genera una contraseña temporal
 * que se muestra una sola vez al administrador.
 */
@Controller
@RequestMapping("/users")
public class UserController {

    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    // RF-07: listado
    @GetMapping
    public String getUsersTemplate(Model model) {
        model.addAttribute("users", service.findAllUsers());
        return "users/list";
    }

    // RF-06: formulario vacío para registrar
    @GetMapping("/new")
    public String getNewUserTemplate(Model model) {
        model.addAttribute("user", new UserDTO(null, "", "", "", "", null, Role.MECHANIC, true));
        model.addAttribute("roles", Role.values());
        return "users/form";
    }

    // RF-07: detalle
    @GetMapping("/{id}")
    public String getUserDetailTemplate(@PathVariable Integer id, Model model,
                                        RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("user", service.findUserById(id));
            return "users/detail";
        } catch (EntityNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/users";
        }
    }

    // RF-07: formulario con los datos actuales para editar
    @GetMapping("/{id}/edit")
    public String getEditUserTemplate(@PathVariable Integer id, Model model,
                                      RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("user", service.findUserById(id));
            model.addAttribute("roles", Role.values());
            return "users/form";
        } catch (EntityNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/users";
        }
    }

    // RF-06: registrar. El sistema genera la contraseña temporal y se muestra una sola vez.
    @PostMapping
    public String createUser(@ModelAttribute UserDTO request, Model model,
                             RedirectAttributes redirectAttributes) {
        try {
            UserDTO created = service.createUser(request);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Usuario " + created.name() + " registrado correctamente");
            // Flash: solo vive hasta la siguiente página; no se guarda en ningún lado
            redirectAttributes.addFlashAttribute("tempPassword", created.password());
            redirectAttributes.addFlashAttribute("tempEmail", created.email());
            return "redirect:/users";
        } catch (IllegalArgumentException e) {
            // Se vuelve al formulario conservando lo escrito
            model.addAttribute("user", request);
            model.addAttribute("roles", Role.values());
            model.addAttribute("errorMessage", e.getMessage());
            return "users/form";
        }
    }

    // RF-07: actualizar (el formulario envía _method=put)
    @PutMapping("/{id}")
    public String updateUser(@PathVariable Integer id, @ModelAttribute UserDTO request,
                             Model model, RedirectAttributes redirectAttributes) {
        try {
            UserDTO updated = service.updateUser(id, request);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Usuario " + updated.name() + " actualizado correctamente");
            return "redirect:/users";
        } catch (EntityNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/users";
        } catch (IllegalArgumentException | IllegalStateException e) {
            // Se recargan documento y correo (no se editan) y se conserva lo que escribió el usuario
            UserDTO current = service.findUserById(id);
            model.addAttribute("user", new UserDTO(id, request.name(), current.document(), current.email(),
                    request.phone(), null, request.role(), request.active()));
            model.addAttribute("roles", Role.values());
            model.addAttribute("errorMessage", e.getMessage());
            return "users/form";
        }
    }

    // Eliminar usuario (el formulario envía _method=delete). Solo administrador (ver SecurityConfig).
    @DeleteMapping("/{id}")
    public String deleteUser(@PathVariable Integer id, Authentication authentication,
                             RedirectAttributes redirectAttributes) {
        try {
            service.deleteUser(id, authentication.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Usuario eliminado correctamente");
        } catch (EntityNotFoundException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/users";
    }
}
