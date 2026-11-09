package dev.tarazonarodriguezrojas.taller1.controller;

import dev.tarazonarodriguezrojas.taller1.model.constant.RepairStatus;
import dev.tarazonarodriguezrojas.taller1.model.dto.RepairDTO;
import dev.tarazonarodriguezrojas.taller1.service.PartService;
import dev.tarazonarodriguezrojas.taller1.service.RepairService;
import dev.tarazonarodriguezrojas.taller1.service.VehicleService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.access.AccessDeniedException;
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
import java.util.List;

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

    @GetMapping("/map")
    public String getMapTemplate(@RequestParam(name = "plate", defaultValue = "") String plate, Model model) {
        String normalized = plate.trim().toUpperCase().replace("-", "").replace(" ", "");
        model.addAttribute("locations", service.findLocations().stream()
                .filter(location -> location.licensePlate().toUpperCase()
                        .replace("-", "").replace(" ", "").contains(normalized))
                .toList());
        model.addAttribute("plate", plate);
        return "repairs/map";
    }

    @GetMapping("/new")
    public String getNewRepairTemplate(Model model) {
        model.addAttribute("repair", new RepairDTO(null, LocalDate.now(), "", RepairStatus.IN_PROGRESS,
                null, null, null, null, null, List.of(), null, null, null, null));
        loadFormData(model);
        return "repairs/form";
    }

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

    @GetMapping("/{id}/edit")
    public String getEditRepairTemplate(@PathVariable Integer id, Model model,
                                        Authentication authentication, RedirectAttributes redirects) {
        try {
            service.assertCanModify(id, authentication.getName(), isAdmin(authentication));
            model.addAttribute("repair", service.findRepairById(id));
            loadFormData(model);
            return "repairs/form";
        } catch (EntityNotFoundException e) {
            redirects.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/repairs";
        }
    }

    @PostMapping
    public String createRepair(@ModelAttribute RepairDTO request,
                               @RequestParam(name = "partIds", required = false) List<Integer> partIds,
                               @RequestParam(name = "quantities", required = false) List<Integer> quantities,
                               Model model, RedirectAttributes redirects) {
        try {
            RepairDTO created = service.createRepair(request, partIds, quantities);
            redirects.addFlashAttribute("successMessage", "Reparación registrada. Costo total: $ " + created.totalCost());
            return "redirect:/repairs";
        } catch (EntityNotFoundException | IllegalArgumentException | IllegalStateException e) {
            model.addAttribute("repair", request);
            model.addAttribute("errorMessage", e.getMessage());
            loadFormData(model);
            return "repairs/form";
        }
    }

    @PutMapping("/{id}")
    public String updateRepair(@PathVariable Integer id, @ModelAttribute RepairDTO request,
                               @RequestParam(name = "partIds", required = false) List<Integer> partIds,
                               @RequestParam(name = "quantities", required = false) List<Integer> quantities,
                               Authentication authentication, Model model, RedirectAttributes redirects) {
        try {
            service.assertCanModify(id, authentication.getName(), isAdmin(authentication));
            service.updateRepair(id, request, partIds, quantities);
            redirects.addFlashAttribute("successMessage", "Reparación actualizada correctamente");
            return "redirect:/repairs";
        } catch (EntityNotFoundException e) {
            redirects.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/repairs";
        } catch (IllegalArgumentException | IllegalStateException e) {
            RepairDTO current = service.findRepairById(id);
            model.addAttribute("repair", new RepairDTO(id, current.date(), request.type(), request.status(),
                    request.laborCost(), current.vehicleId(), current.licensePlate(), current.mechanicId(),
                    current.mechanicName(), current.items(), current.totalCost(),
                    request.workshopName(), request.workshopLatitude(), request.workshopLongitude()));
            model.addAttribute("errorMessage", e.getMessage());
            loadFormData(model);
            return "repairs/form";
        }
    }

    @DeleteMapping("/{id}")
    public String deleteRepair(@PathVariable Integer id, RedirectAttributes redirects) {
        try {
            service.deleteRepair(id);
            redirects.addFlashAttribute("successMessage", "Reparación eliminada correctamente");
        } catch (EntityNotFoundException e) {
            redirects.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/repairs";
    }

    @GetMapping("/{id}/location")
    public String getLocationTemplate(@PathVariable Integer id, Authentication authentication,
                                      Model model, RedirectAttributes redirects) {
        try {
            service.assertCanModify(id, authentication.getName(), isAdmin(authentication));
            model.addAttribute("repair", service.findRepairById(id));
            return "repairs/location";
        } catch (EntityNotFoundException e) {
            redirects.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/repairs";
        }
    }

    @PutMapping("/{id}/location")
    public String saveLocation(@PathVariable Integer id,
                               @RequestParam(name = "workshopName", required = false) String workshopName,
                               @RequestParam(name = "latitude", required = false) Double latitude,
                               @RequestParam(name = "longitude", required = false) Double longitude,
                               Authentication authentication, Model model, RedirectAttributes redirects) {
        service.assertCanModify(id, authentication.getName(), isAdmin(authentication));
        try {
            service.saveLocation(id, workshopName, latitude, longitude);
            redirects.addFlashAttribute("successMessage", "Ubicación guardada correctamente");
            return "redirect:/repairs/" + id;
        } catch (EntityNotFoundException e) {
            redirects.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/repairs";
        } catch (IllegalArgumentException e) {
            RepairDTO current = service.findRepairById(id);
            model.addAttribute("repair", new RepairDTO(id, current.date(), current.type(), current.status(),
                    current.laborCost(), current.vehicleId(), current.licensePlate(), current.mechanicId(),
                    current.mechanicName(), current.items(), current.totalCost(), workshopName, latitude, longitude));
            model.addAttribute("errorMessage", e.getMessage());
            return "repairs/location";
        }
    }

    private void loadFormData(Model model) {
        model.addAttribute("vehicles", vehicleService.findAllVehicles("", ""));
        model.addAttribute("mechanics", service.findMechanics());
        model.addAttribute("parts", partService.findAllParts());
        model.addAttribute("statuses", RepairStatus.values());
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}
