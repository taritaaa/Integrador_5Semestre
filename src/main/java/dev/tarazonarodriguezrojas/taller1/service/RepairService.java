package dev.tarazonarodriguezrojas.taller1.service;

import dev.tarazonarodriguezrojas.taller1.model.constant.RepairStatus;
import dev.tarazonarodriguezrojas.taller1.model.constant.Role;
import dev.tarazonarodriguezrojas.taller1.model.dto.LocationDTO;
import dev.tarazonarodriguezrojas.taller1.model.dto.MechanicDTO;
import dev.tarazonarodriguezrojas.taller1.model.dto.RepairDTO;
import dev.tarazonarodriguezrojas.taller1.model.dto.RepairItemDTO;
import dev.tarazonarodriguezrojas.taller1.model.entity.Part;
import dev.tarazonarodriguezrojas.taller1.model.entity.Repair;
import dev.tarazonarodriguezrojas.taller1.model.entity.RepairPart;
import dev.tarazonarodriguezrojas.taller1.model.entity.User;
import dev.tarazonarodriguezrojas.taller1.model.entity.Vehicle;
import dev.tarazonarodriguezrojas.taller1.repository.PartRepository;
import dev.tarazonarodriguezrojas.taller1.repository.RepairPartRepository;
import dev.tarazonarodriguezrojas.taller1.repository.RepairRepository;
import dev.tarazonarodriguezrojas.taller1.repository.UserRepository;
import dev.tarazonarodriguezrojas.taller1.repository.VehicleRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class RepairService {

    private static final BigDecimal MAX_LABOR_COST = new BigDecimal("99999999.99");

    private final RepairRepository repository;
    private final RepairPartRepository repairPartRepository;
    private final VehicleRepository vehicleRepository;
    private final UserRepository userRepository;
    private final PartRepository partRepository;
    private final PartService partService;

    public RepairService(RepairRepository repository, RepairPartRepository repairPartRepository,
                         VehicleRepository vehicleRepository, UserRepository userRepository,
                         PartRepository partRepository, PartService partService) {
        this.repository = repository;
        this.repairPartRepository = repairPartRepository;
        this.vehicleRepository = vehicleRepository;
        this.userRepository = userRepository;
        this.partRepository = partRepository;
        this.partService = partService;
    }

    @Transactional(readOnly = true)
    public List<RepairDTO> findAllRepairs(String plate, RepairStatus status) {
        String filter = plate == null ? "" : plate.trim().toUpperCase().replace("-", "").replace(" ", "");
        List<Repair> repairs = status == null
                ? repository.searchByPlate(filter) : repository.searchByPlateAndStatus(filter, status);
        return repairs.stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public RepairDTO findRepairById(Integer id) {
        return toDTO(findEntity(id));
    }

    @Transactional(readOnly = true)
    public List<MechanicDTO> findMechanics() {
        return userRepository.findByRoleAndActiveTrue(Role.MECHANIC).stream()
                .map(u -> new MechanicDTO(u.getId(), u.getName())).toList();
    }

    @Transactional(readOnly = true)
    public void assertCanModify(Integer id, String email, boolean admin) {
        Repair repair = findEntity(id);
        if (!admin && !repair.getMechanic().getEmail().equalsIgnoreCase(email)) {
            throw new AccessDeniedException("Solo puede modificar las reparaciones que tiene asignadas");
        }
    }

    // Recibe listas paralelas enviadas por las filas del formulario.
    public RepairDTO createRepair(RepairDTO request, List<Integer> partIds, List<Integer> quantities) {
        validateCommon(request);
        if (request.date() == null || request.vehicleId() == null || request.mechanicId() == null) {
            throw new IllegalArgumentException("La fecha, el vehículo y el mecánico son obligatorios");
        }
        Vehicle vehicle = vehicleRepository.findById(request.vehicleId())
                .orElseThrow(() -> new EntityNotFoundException("Vehículo no encontrado"));
        User mechanic = userRepository.findById(request.mechanicId())
                .filter(u -> u.getRole() == Role.MECHANIC && u.isActive())
                .orElseThrow(() -> new IllegalArgumentException("El mecánico no existe o está inactivo"));

        validateLocation(request.workshopName(), request.workshopLatitude(), request.workshopLongitude());
        List<RepairItemDTO> requestedItems = validateItems(partIds, quantities, vehicle);
        Repair repair = new Repair();
        repair.setDate(request.date());
        repair.setType(request.type().trim());
        repair.setStatus(request.status());
        repair.setLaborCost(request.laborCost().setScale(2, RoundingMode.HALF_UP));
        repair.setVehicle(vehicle);
        repair.setMechanic(mechanic);
        applyLocation(repair, request.workshopName(), request.workshopLatitude(), request.workshopLongitude());
        Repair saved = repository.save(repair);

        for (RepairItemDTO item : requestedItems) {
            Part part = partRepository.findById(item.partId())
                    .orElseThrow(() -> new EntityNotFoundException("Repuesto no encontrado"));
            if (consumesStock(request.status())) {
                partService.decreaseStock(part.getId(), item.quantity());
            }
            RepairPart relation = new RepairPart(saved, part, item.quantity(), part.getPrice());
            saved.getRepairParts().add(repairPartRepository.save(relation));
        }
        return toDTO(saved);
    }

    // Actualiza los repuestos sin perder su precio histórico si permanecen iguales.
    public RepairDTO updateRepair(Integer id, RepairDTO request,
                                  List<Integer> partIds, List<Integer> quantities) {
        Repair repair = findEntity(id);
        validateCommon(request);
        validateLocation(request.workshopName(), request.workshopLatitude(), request.workshopLongitude());
        List<RepairItemDTO> requested = validateItems(partIds, quantities, repair.getVehicle());
        boolean oldConsumes = consumesStock(repair.getStatus());
        boolean newConsumes = consumesStock(request.status());

        List<RepairPart> oldRelations = new ArrayList<>(repair.getRepairParts());
        // Eliminar los repuestos que ya no se usarán.
        for (RepairPart old : oldRelations) {
            boolean remains = requested.stream()
                    .anyMatch(item -> item.partId().equals(old.getPart().getId()));
            if (!remains) {
                if (oldConsumes) {
                    partService.restoreStock(old.getPart().getId(), old.getQuantity());
                }
                repairPartRepository.delete(old);
                repair.getRepairParts().remove(old);
            }
        }

        // Actualizar cantidades, agregar nuevos repuestos y ajustar el inventario.
        for (RepairItemDTO item : requested) {
            RepairPart existing = null;
            for (RepairPart old : repair.getRepairParts()) {
                if (old.getPart().getId().equals(item.partId())) {
                    existing = old;
                    break;
                }
            }
            int previousQuantity = existing == null ? 0 : existing.getQuantity();
            int deductedBefore = oldConsumes ? previousQuantity : 0;
            int deductedAfter = newConsumes ? item.quantity() : 0;
            int difference = deductedAfter - deductedBefore;
            if (difference > 0) {
                partService.decreaseStock(item.partId(), difference);
            } else if (difference < 0) {
                partService.restoreStock(item.partId(), -difference);
            }
            if (existing == null) {
                Part part = partRepository.findById(item.partId())
                        .orElseThrow(() -> new EntityNotFoundException("Repuesto no encontrado"));
                RepairPart relation = new RepairPart(repair, part, item.quantity(), part.getPrice());
                repair.getRepairParts().add(repairPartRepository.save(relation));
            } else {
                existing.setQuantity(item.quantity());
            }
        }
        repair.setType(request.type().trim());
        repair.setStatus(request.status());
        repair.setLaborCost(request.laborCost().setScale(2, RoundingMode.HALF_UP));
        applyLocation(repair, request.workshopName(), request.workshopLatitude(), request.workshopLongitude());
        return toDTO(repository.save(repair));
    }

    public void deleteRepair(Integer id) {
        Repair repair = findEntity(id);
        List<RepairPart> relations = new ArrayList<>(repair.getRepairParts());
        if (consumesStock(repair.getStatus())) {
            for (RepairPart item : relations) {
                partService.restoreStock(item.getPart().getId(), item.getQuantity());
            }
        }
        repairPartRepository.deleteAll(relations);
        repair.getRepairParts().clear();
        repository.delete(repair);
    }

    // Lugar donde se realizó la reparación.
    public RepairDTO saveLocation(Integer id, String workshopName, Double latitude, Double longitude) {
        Repair repair = findEntity(id);
        validateLocation(workshopName, latitude, longitude);
        applyLocation(repair, workshopName, latitude, longitude);
        return toDTO(repository.save(repair));
    }

    private void validateLocation(String workshopName, Double latitude, Double longitude) {
        if (workshopName == null || workshopName.isBlank() || workshopName.trim().length() > 100) {
            throw new IllegalArgumentException("Ingrese el nombre del taller (máximo 100 caracteres)");
        }
        if (latitude == null || longitude == null || !Double.isFinite(latitude)
                || !Double.isFinite(longitude) || latitude < -90 || latitude > 90
                || longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("Ingrese coordenadas válidas: latitud entre -90 y 90, longitud entre -180 y 180");
        }
    }

    private void applyLocation(Repair repair, String workshopName, Double latitude, Double longitude) {
        repair.setWorkshopName(workshopName.trim());
        repair.setWorkshopLatitude(latitude);
        repair.setWorkshopLongitude(longitude);
    }

    @Transactional(readOnly = true)
    public List<LocationDTO> findLocations() {
        return repository.findByWorkshopLatitudeIsNotNullAndWorkshopLongitudeIsNotNull().stream()
                .map(r -> new LocationDTO(r.getId(), r.getVehicle().getLicensePlate(), r.getType(),
                        r.getStatus(), r.getWorkshopName(), r.getWorkshopLatitude(), r.getWorkshopLongitude()))
                .toList();
    }

    private List<RepairItemDTO> validateItems(List<Integer> partIds, List<Integer> quantities, Vehicle vehicle) {
        List<RepairItemDTO> result = new ArrayList<>();
        if (partIds == null || partIds.isEmpty()) {
            if (quantities != null && !quantities.isEmpty()) {
                throw new IllegalArgumentException("Seleccione el repuesto correspondiente a cada cantidad");
            }
            return result;
        }
        if (quantities == null || partIds.size() != quantities.size()) {
            throw new IllegalArgumentException("Cada repuesto debe tener una cantidad");
        }
        Set<Integer> used = new HashSet<>();
        for (int i = 0; i < partIds.size(); i++) {
            Integer partId = partIds.get(i);
            Integer quantity = quantities.get(i);
            if (partId == null || quantity == null || quantity < 1) {
                throw new IllegalArgumentException("Seleccione un repuesto y una cantidad mayor que cero");
            }
            if (!used.add(partId)) {
                throw new IllegalArgumentException("No repita el mismo repuesto en una reparación");
            }
            Part part = partRepository.findById(partId)
                    .orElseThrow(() -> new EntityNotFoundException("Repuesto con id " + partId + " no encontrado"));
            if (!part.getModel().getId().equals(vehicle.getModel().getId())) {
                throw new IllegalStateException("El repuesto " + part.getName() + " no es compatible con el vehículo");
            }
            result.add(new RepairItemDTO(partId, part.getName(), quantity, part.getPrice(),
                    part.getPrice().multiply(BigDecimal.valueOf(quantity))));
        }
        return result;
    }

    private Repair findEntity(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Reparación con id " + id + " no encontrada"));
    }

    private boolean consumesStock(RepairStatus status) {
        return status != RepairStatus.CANCELLED;
    }

    private RepairDTO toDTO(Repair repair) {
        List<RepairItemDTO> items = new ArrayList<>();
        BigDecimal total = repair.getLaborCost();
        for (RepairPart item : repair.getRepairParts()) {
            BigDecimal subtotal = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            items.add(new RepairItemDTO(item.getPart().getId(), item.getPart().getName(),
                    item.getQuantity(), item.getUnitPrice(), subtotal));
            total = total.add(subtotal);
        }
        return new RepairDTO(repair.getId(), repair.getDate(), repair.getType(), repair.getStatus(),
                repair.getLaborCost(), repair.getVehicle().getId(), repair.getVehicle().getLicensePlate(),
                repair.getMechanic().getId(), repair.getMechanic().getName(), items, total,
                repair.getWorkshopName(), repair.getWorkshopLatitude(), repair.getWorkshopLongitude());
    }

    private void validateCommon(RepairDTO request) {
        if (request.type() == null || request.type().isBlank() || request.type().trim().length() > 100) {
            throw new IllegalArgumentException("Ingrese un tipo de reparación válido (máximo 100 caracteres)");
        }
        if (request.status() == null) {
            throw new IllegalArgumentException("El estado es obligatorio");
        }
        if (request.laborCost() == null || request.laborCost().compareTo(BigDecimal.ZERO) < 0
                || request.laborCost().compareTo(MAX_LABOR_COST) > 0) {
            throw new IllegalArgumentException("El costo de mano de obra debe estar entre 0 y 99.999.999,99");
        }
    }
}
