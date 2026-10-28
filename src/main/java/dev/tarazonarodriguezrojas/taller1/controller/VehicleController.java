package dev.tarazonarodriguezrojas.taller1.controller;

import dev.tarazonarodriguezrojas.taller1.model.constant.PlateColor;
import dev.tarazonarodriguezrojas.taller1.model.dto.PlateModelDTO;
import dev.tarazonarodriguezrojas.taller1.model.dto.VehicleDTO;
import dev.tarazonarodriguezrojas.taller1.service.VehicleModelService;
import dev.tarazonarodriguezrojas.taller1.service.VehicleService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/vehicles")
public class VehicleController {

    private final VehicleService service;
    private final VehicleModelService modelService;

    public VehicleController(VehicleService service, VehicleModelService modelService) {
        this.service = service;
        this.modelService = modelService;
    }

    // RF-13: listado con filtros opcionales (?plate=...&owner=...)
    @GetMapping
    public String getVehiclesTemplate(@RequestParam(name = "plate", defaultValue = "") String plate,
                                      @RequestParam(name = "owner", defaultValue = "") String owner,
                                      Model model) {
        model.addAttribute("vehicles", service.findAllVehicles(plate, owner));
        // Se devuelven los filtros para que el formulario conserve lo escrito
        model.addAttribute("plate", plate);
        model.addAttribute("owner", owner);
        return "vehicles/list";
    }

    // RF-27: pantalla para buscar el modelo y la marca por placa
    @GetMapping("/model-by-plate")
    public String getModelByPlateTemplate(@RequestParam(name = "plate", required = false) String plate,
                                          Model model) {
        // Si no se escribió ninguna placa, solo se muestra el formulario vacío
        if (plate == null || plate.isBlank()) {
            return "vehicles/model-by-plate";
        }
        model.addAttribute("plate", plate);
        try {
            PlateModelDTO result = service.findModelByPlate(plate);
            model.addAttribute("result", result);
        } catch (EntityNotFoundException | IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
        }
        return "vehicles/model-by-plate";
    }

    // RF-12: formulario vacío para registrar
    @GetMapping("/new")
    public String getNewVehicleTemplate(Model model) {
        model.addAttribute("vehicle", new VehicleDTO(null, "", null, "", "", "", null, null, null));
        loadFormData(model);
        return "vehicles/form";
    }

    // RF-13: detalle
    @GetMapping("/{id}")
    public String getVehicleDetailTemplate(@PathVariable Integer id, Model model,
                                           RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("vehicle", service.findVehicleById(id));
            return "vehicles/detail";
        } catch (EntityNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/vehicles";
        }
    }

    // RF-14: formulario con los datos actuales para editar
    @GetMapping("/{id}/edit")
    public String getEditVehicleTemplate(@PathVariable Integer id, Model model,
                                         RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("vehicle", service.findVehicleById(id));
            loadFormData(model);
            return "vehicles/form";
        } catch (EntityNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/vehicles";
        }
    }

    // RF-12: registrar
    @PostMapping
    public String createVehicle(@ModelAttribute VehicleDTO request, Model model,
                                RedirectAttributes redirectAttributes) {
        try {
            VehicleDTO created = service.createVehicle(request);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Vehículo " + created.licensePlate() + " registrado correctamente");
            return "redirect:/vehicles";
        } catch (EntityNotFoundException | IllegalArgumentException e) {
            // Se vuelve al formulario conservando lo que escribió el usuario
            model.addAttribute("vehicle", request);
            model.addAttribute("errorMessage", e.getMessage());
            loadFormData(model);
            return "vehicles/form";
        }
    }

    // RF-14: actualizar (el formulario envía _method=put)
    @PutMapping("/{id}")
    public String updateVehicle(@PathVariable Integer id, @ModelAttribute VehicleDTO request,
                                Model model, RedirectAttributes redirectAttributes) {
        try {
            VehicleDTO updated = service.updateVehicle(id, request);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Vehículo " + updated.licensePlate() + " actualizado correctamente");
            return "redirect:/vehicles";
        } catch (EntityNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/vehicles";
        } catch (IllegalArgumentException e) {
            model.addAttribute("vehicle", new VehicleDTO(id, request.licensePlate(), request.plateColor(),
                    request.ownerName(), request.ownerDocument(), request.ownerPhone(),
                    request.modelId(), null, null));
            model.addAttribute("errorMessage", e.getMessage());
            loadFormData(model);
            return "vehicles/form";
        }
    }

    // RF-15: eliminar (el formulario envía _method=delete)
    @DeleteMapping("/{id}")
    public String deleteVehicle(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            service.deleteVehicle(id);
            redirectAttributes.addFlashAttribute("successMessage", "Vehículo eliminado correctamente");
        } catch (EntityNotFoundException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/vehicles";
    }

    // Datos que necesita el formulario: lista de modelos y colores de placa
    private void loadFormData(Model model) {
        model.addAttribute("models", modelService.findAllModels());
        model.addAttribute("plateColors", PlateColor.values());
    }
}