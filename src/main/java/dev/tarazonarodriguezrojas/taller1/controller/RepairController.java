package dev.tarazonarodriguezrojas.taller1.controller;

import dev.tarazonarodriguezrojas.taller1.model.constant.RepairStatus;
import dev.tarazonarodriguezrojas.taller1.model.dto.RepairDTO;
import dev.tarazonarodriguezrojas.taller1.service.PartService;
import dev.tarazonarodriguezrojas.taller1.service.RepairService;
import dev.tarazonarodriguezrojas.taller1.service.VehicleService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

/**
 * Pantallas de reparaciones (RF-21 a RF-24) y ubicación de la tienda en el mapa (RF-28).
 * El descuento y la devolución de stock (RF-25) ocurren dentro del service.
 * Registrar, consultar y actualizar: administrador y mecánico (el mecánico solo las que tiene asignadas).
 * Eliminar: solo administrador (ver SecurityConfig).
 */
@Controller
@RequestMapping("/repairs")
public class RepairController {

    private final RepairService service;
    private final VehicleService vehicleService;
    private final PartService partService;

    public RepairController(RepairService service, VehicleService vehicleService, PartService partService) {
        this.service = service;
        this.vehicleService = vehicleService;
        this.partService = partService;
    }

    // RF-22: historial con filtros opcionales (?plate=...&status=...)
    @GetMapping
    public String getRepairsTemplate(@RequestParam(name = "plate", defaultValue = "") String plate,
                                     @RequestParam(name = "status", required = false) RepairStatus status,
                                     Model model) {
        model.addAttribute("repairs", service.findAllRepairs(plate, status));
        model.addAttribute("plate", plate);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("statuses", RepairStatus.values());
        return "repairs/list";
    }

    // RF-28: mapa con todas las tiendas marcadas
    @GetMapping("/map")
    public String getMapTemplate(Model model) {
        model.addAttribute("locations", service.findLocations());
        return "repairs/map";
    }

    // RF-21: formulario vacío para registrar
    @GetMapping("/new")
    public String getNewRepairTemplate(Model model) {
        model.addAttribute("repair", new RepairDTO(null, LocalDate.now(), "", RepairStatus.IN_PROGRESS,
                null, null, null, null, null, null, null, null, null, null, null, null, null));
        loadFormData(model);
        return "repairs/form";
    }

    // RF-22: detalle
    @GetMapping("/{id}")
    public String getRepairDetailTemplate(@PathVariable Integer id, Model model,
                                          RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("repair", service.findRepairById(id));
            return "repairs/detail";
        } catch (EntityNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/repairs";
        }
    }

    // RF-23: formulario con los datos actuales para editar.
    // El mecánico solo puede editar las reparaciones que tiene asignadas.
    @GetMapping("/{id}/edit")
    public String getEditRepairTemplate(@PathVariable Integer id, Model model,
                                        Authentication authentication,
                                        RedirectAttributes redirectAttributes) {
        try {
            service.assertCanModify(id, authentication.getName(), isAdmin(authentication));
            model.addAttribute("repair", service.findRepairById(id));
            loadFormData(model);
            return "repairs/form";
        } catch (EntityNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/repairs";
        }
    }

    // RF-21 y RF-25: registrar
    @PostMapping
    public String createRepair(@ModelAttribute RepairDTO request, Model model,
                               RedirectAttributes redirectAttributes) {
        try {
            RepairDTO created = service.createRepair(request);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Reparación registrada correctamente. Costo total: $ " + created.totalCost());
            return "redirect:/repairs";
        } catch (EntityNotFoundException | IllegalArgumentException | IllegalStateException e) {
            // Se vuelve al formulario conservando lo que escribió el usuario
            model.addAttribute("repair", request);
            model.addAttribute("errorMessage", e.getMessage());
            loadFormData(model);
            return "repairs/form";
        }
    }

    // RF-23 y RF-25: actualizar (el formulario envía _method=put)
    @PutMapping("/{id}")
    public String updateRepair(@PathVariable Integer id, @ModelAttribute RepairDTO request,
                               Model model, Authentication authentication,
                               RedirectAttributes redirectAttributes) {
        try {
            service.assertCanModify(id, authentication.getName(), isAdmin(authentication));
            service.updateRepair(id, request);
            redirectAttributes.addFlashAttribute("successMessage", "Reparación actualizada correctamente");
            return "redirect:/repairs";
        } catch (EntityNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/repairs";
        } catch (IllegalArgumentException | IllegalStateException e) {
            // Se recargan los datos que no se editan y se conserva lo que escribió el usuario
            RepairDTO current = service.findRepairById(id);
            model.addAttribute("repair", new RepairDTO(id, current.date(), request.type(), request.status(),
                    request.laborCost(), current.vehicleId(), current.licensePlate(),
                    current.mechanicId(), current.mechanicName(), current.partId(), current.partName(),
                    current.quantity(), current.unitPrice(), current.totalCost(),
                    current.workshopName(), current.workshopLatitude(), current.workshopLongitude()));
            model.addAttribute("errorMessage", e.getMessage());
            loadFormData(model);
            return "repairs/form";
        }
    }

    // RF-24: eliminar (el formulario envía _method=delete). Solo administrador.
    @DeleteMapping("/{id}")
    public String deleteRepair(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            service.deleteRepair(id);
            redirectAttributes.addFlashAttribute("successMessage", "Reparación eliminada correctamente");
        } catch (EntityNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/repairs";
    }

    // RF-28: pantalla para marcar la tienda en el mapa
    @GetMapping("/{id}/location")
    public String getLocationTemplate(@PathVariable Integer id, Model model,
                                      RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("repair", service.findRepairById(id));
            return "repairs/location";
        } catch (EntityNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/repairs";
        }
    }

    // RF-28: guardar la ubicación (el formulario envía _method=put)
    @PutMapping("/{id}/location")
    public String saveLocation(@PathVariable Integer id,
                               @RequestParam(name = "workshopName", required = false) String workshopName,
                               @RequestParam(name = "latitude", required = false) Double latitude,
                               @RequestParam(name = "longitude", required = false) Double longitude,
                               Model model, RedirectAttributes redirectAttributes) {
        try {
            service.saveLocation(id, workshopName, latitude, longitude);
            redirectAttributes.addFlashAttribute("successMessage", "Ubicación guardada correctamente");
            return "redirect:/repairs/" + id;
        } catch (EntityNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/repairs";
        } catch (IllegalArgumentException e) {
            RepairDTO current = service.findRepairById(id);
            model.addAttribute("repair", new RepairDTO(id, current.date(), current.type(), current.status(),
                    current.laborCost(), current.vehicleId(), current.licensePlate(),
                    current.mechanicId(), current.mechanicName(), current.partId(), current.partName(),
                    current.quantity(), current.unitPrice(), current.totalCost(),
                    workshopName, latitude, longitude));
            model.addAttribute("errorMessage", e.getMessage());
            return "repairs/location";
        }
    }

    // Datos que necesita el formulario: vehículos, mecánicos, repuestos y estados
    private void loadFormData(Model model) {
        model.addAttribute("vehicles", vehicleService.findAllVehicles("", ""));
        model.addAttribute("mechanics", service.findMechanics());
        model.addAttribute("parts", partService.findAllParts());
        model.addAttribute("statuses", RepairStatus.values());
    }

    // ¿El usuario que hace la petición es administrador?
    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}