package dev.tarazonarodriguezrojas.taller1.controller;

import dev.tarazonarodriguezrojas.taller1.model.dto.PartDTO;
import dev.tarazonarodriguezrojas.taller1.service.PartService;
import dev.tarazonarodriguezrojas.taller1.service.VehicleModelService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Pantallas de repuestos (RF-16 a RF-20) y búsqueda de repuestos compatibles (RF-26).
 * Consultar y buscar compatibles: administrador y mecánico.
 * Registrar, actualizar, eliminar y subir imagen: solo administrador
 * (se aplicará con la seguridad, RF-05).
 */
@Controller
@RequestMapping("/parts")
public class PartController {

    private final PartService service;
    private final VehicleModelService modelService;

    public PartController(PartService service, VehicleModelService modelService) {
        this.service = service;
        this.modelService = modelService;
    }

    // RF-17: listado
    @GetMapping
    public String getPartsTemplate(Model model) {
        model.addAttribute("parts", service.findAllParts());
        return "parts/list";
    }

    // RF-26: pantalla para buscar los repuestos compatibles con un modelo
    @GetMapping("/compatible")
    public String getCompatiblePartsTemplate(@RequestParam(name = "modelId", required = false) Integer modelId,
                                             Model model) {
        model.addAttribute("models", modelService.findAllModels());
        // Si no se eligió un modelo, solo se muestra el formulario
        if (modelId == null) {
            return "parts/compatible";
        }
        model.addAttribute("selectedModelId", modelId);
        try {
            model.addAttribute("parts", service.findCompatibleParts(modelId));
            model.addAttribute("searched", true);
        } catch (EntityNotFoundException | IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
        }
        return "parts/compatible";
    }

    // RF-16: formulario vacío para registrar
    @GetMapping("/new")
    public String getNewPartTemplate(Model model) {
        model.addAttribute("part", new PartDTO(null, "", "", null, "", null, null, null, null, null));
        loadFormData(model);
        return "parts/form";
    }

    // RF-17: detalle (incluye la imagen y el formulario para cambiarla)
    @GetMapping("/{id}")
    public String getPartDetailTemplate(@PathVariable Integer id, Model model,
                                        RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("part", service.findPartById(id));
            return "parts/detail";
        } catch (EntityNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/parts";
        }
    }

    // RF-18: formulario con los datos actuales para editar
    @GetMapping("/{id}/edit")
    public String getEditPartTemplate(@PathVariable Integer id, Model model,
                                      RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("part", service.findPartById(id));
            loadFormData(model);
            return "parts/form";
        } catch (EntityNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/parts";
        }
    }

    // RF-16: registrar (el formulario es multipart porque puede traer la imagen)
    @PostMapping
    public String createPart(@ModelAttribute PartDTO request,
                             @RequestParam(name = "image", required = false) MultipartFile image,
                             Model model, RedirectAttributes redirectAttributes) {
        try {
            PartDTO created = service.createPart(request, image);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Repuesto " + created.name() + " registrado correctamente");
            return "redirect:/parts";
        } catch (EntityNotFoundException | IllegalArgumentException e) {
            // Se vuelve al formulario conservando lo que escribió el usuario
            model.addAttribute("part", request);
            model.addAttribute("errorMessage", e.getMessage());
            loadFormData(model);
            return "parts/form";
        }
    }

    // RF-18: actualizar (el formulario envía _method=put)
    @PutMapping("/{id}")
    public String updatePart(@PathVariable Integer id, @ModelAttribute PartDTO request,
                             Model model, RedirectAttributes redirectAttributes) {
        try {
            PartDTO updated = service.updatePart(id, request);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Repuesto " + updated.name() + " actualizado correctamente");
            return "redirect:/parts";
        } catch (EntityNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/parts";
        } catch (IllegalArgumentException e) {
            model.addAttribute("part", new PartDTO(id, request.name(), request.description(),
                    request.price(), request.ean(), request.stock(), null,
                    request.modelId(), null, null));
            model.addAttribute("errorMessage", e.getMessage());
            loadFormData(model);
            return "parts/form";
        }
    }

    // RF-19: eliminar (el formulario envía _method=delete)
    @DeleteMapping("/{id}")
    public String deletePart(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            service.deletePart(id);
            redirectAttributes.addFlashAttribute("successMessage", "Repuesto eliminado correctamente");
        } catch (EntityNotFoundException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/parts";
    }

    // RF-20: adjuntar o reemplazar la imagen de referencia
    @PostMapping("/{id}/image")
    public String uploadImage(@PathVariable Integer id, @RequestParam("image") MultipartFile image,
                              RedirectAttributes redirectAttributes) {
        try {
            service.saveImage(id, image);
            redirectAttributes.addFlashAttribute("successMessage", "Imagen guardada correctamente");
        } catch (EntityNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/parts";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/parts/" + id;
    }

    // Datos que necesita el formulario: lista de modelos
    private void loadFormData(Model model) {
        model.addAttribute("models", modelService.findAllModels());
    }
}