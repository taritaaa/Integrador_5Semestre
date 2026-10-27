package dev.tarazonarodriguezrojas.taller1.controller;

import dev.tarazonarodriguezrojas.taller1.model.dto.VehicleModelDTO;
import dev.tarazonarodriguezrojas.taller1.service.VehicleModelService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Pantallas de modelos de vehículo (RF-08 a RF-11).
 * Consultar: administrador y mecánico. Registrar, actualizar y eliminar: solo administrador.
 * Esa restricción por rol (RF-05) se aplicará cuando se agregue la seguridad.
 */
@Controller
@RequestMapping("/models")
public class VehicleModelController {

    private final VehicleModelService service;

    public VehicleModelController(VehicleModelService service) {
        this.service = service;
    }

    // RF-09: listado
    @GetMapping
    public String getModelsTemplate(Model model) {
        model.addAttribute("models", service.findAllModels());
        return "models/list";
    }

    // RF-08: formulario vacío para registrar
    @GetMapping("/new")
    public String getNewModelTemplate(Model model) {
        model.addAttribute("vehicleModel", new VehicleModelDTO(null, "", ""));
        return "models/form";
    }

    // RF-09: detalle
    @GetMapping("/{id}")
    public String getModelDetailTemplate(@PathVariable Integer id, Model model,
                                         RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("vehicleModel", service.findModelById(id));
            return "models/detail";
        } catch (EntityNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/models";
        }
    }

    // RF-10: formulario con los datos actuales para editar
    @GetMapping("/{id}/edit")
    public String getEditModelTemplate(@PathVariable Integer id, Model model,
                                       RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("vehicleModel", service.findModelById(id));
            return "models/form";
        } catch (EntityNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/models";
        }
    }

    // RF-08: registrar
    @PostMapping
    public String createModel(@ModelAttribute VehicleModelDTO request, Model model,
                              RedirectAttributes redirectAttributes) {
        try {
            VehicleModelDTO created = service.createModel(request);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Modelo " + created.name() + " registrado correctamente");
            return "redirect:/models";
        } catch (IllegalArgumentException e) {
            // Se vuelve al formulario conservando lo que escribió el usuario
            model.addAttribute("vehicleModel", request);
            model.addAttribute("errorMessage", e.getMessage());
            return "models/form";
        }
    }

    // RF-10: actualizar (el formulario envía _method=put)
    @PutMapping("/{id}")
    public String updateModel(@PathVariable Integer id, @ModelAttribute VehicleModelDTO request,
                              Model model, RedirectAttributes redirectAttributes) {
        try {
            VehicleModelDTO updated = service.updateModel(id, request);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Modelo " + updated.name() + " actualizado correctamente");
            return "redirect:/models";
        } catch (EntityNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/models";
        } catch (IllegalArgumentException e) {
            model.addAttribute("vehicleModel", new VehicleModelDTO(id, request.name(), request.brand()));
            model.addAttribute("errorMessage", e.getMessage());
            return "models/form";
        }
    }

    // RF-11: eliminar (el formulario envía _method=delete)
    @DeleteMapping("/{id}")
    public String deleteModel(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            service.deleteModel(id);
            redirectAttributes.addFlashAttribute("successMessage", "Modelo eliminado correctamente");
        } catch (EntityNotFoundException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/models";
    }
}