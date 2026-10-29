package dev.tarazonarodriguezrojas.taller1.service;

import dev.tarazonarodriguezrojas.taller1.model.constant.RepairStatus;
import dev.tarazonarodriguezrojas.taller1.model.constant.Role;
import dev.tarazonarodriguezrojas.taller1.model.dto.LocationDTO;
import dev.tarazonarodriguezrojas.taller1.model.dto.MechanicDTO;
import dev.tarazonarodriguezrojas.taller1.model.dto.RepairDTO;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class RepairService {

    // La columna labor_cost es DECIMAL(10,2): máximo 8 dígitos enteros y 2 decimales
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

    // RF-22: historial de reparaciones, con filtros opcionales por placa y por estado
    @Transactional(readOnly = true)
    public List<RepairDTO> findAllRepairs(String plate, RepairStatus status) {
        // El filtro de placa se limpia: mayúsculas y sin guiones ni espacios
        String plateFilter = plate == null ? "" : plate.trim().toUpperCase().replace("-", "").replace(" ", "");
        List<Repair> repairs = status == null
                ? repository.searchByPlate(plateFilter)
                : repository.searchByPlateAndStatus(plateFilter, status);
        return repairs.stream().map(this::toDTO).toList();
    }

    // RF-22: detalle de una reparación
    @Transactional(readOnly = true)
    public RepairDTO findRepairById(Integer repairId) throws EntityNotFoundException {
        return toDTO(findEntity(repairId));
    }

    // Mecánicos activos, para la lista desplegable del formulario
    @Transactional(readOnly = true)
    public List<MechanicDTO> findMechanics() {
        return userRepository.findByRoleAndActiveTrue(Role.MECHANIC).stream()
                .map(u -> new MechanicDTO(u.getId(), u.getName()))
                .toList();
    }

    // RF-21 y RF-25: registrar reparación. Si usa un repuesto, valida compatibilidad y stock
    // y descuenta la cantidad. Si algo falla, no se guarda nada.
    public RepairDTO createRepair(RepairDTO request)
            throws EntityNotFoundException, IllegalArgumentException, IllegalStateException {
        validateCommon(request);
        if (request.date() == null) {
            throw new IllegalArgumentException("La fecha es obligatoria");
        }
        if (request.vehicleId() == null) {
            throw new IllegalArgumentException("Debe seleccionar un vehículo");
        }
        if (request.mechanicId() == null) {
            throw new IllegalArgumentException("Debe seleccionar un mecánico");
        }

        Vehicle vehicle = vehicleRepository.findById(request.vehicleId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Vehículo con id " + request.vehicleId() + " no encontrado"));

        User mechanic = userRepository.findById(request.mechanicId())
                .filter(u -> u.getRole() == Role.MECHANIC && u.isActive())
                .orElseThrow(() -> new IllegalArgumentException(
                        "El mecánico seleccionado no existe o está inactivo"));

        // Repuesto opcional
        Part part = null;
        Integer quantity = null;
        if (request.partId() != null) {
            quantity = request.quantity();
            if (quantity == null || quantity < 1) {
                throw new IllegalArgumentException("La cantidad de repuesto debe ser al menos 1");
            }
            part = partRepository.findById(request.partId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Repuesto con id " + request.partId() + " no encontrado"));

            // RF-25: el repuesto debe ser compatible con el modelo del vehículo
            if (!part.getModel().getId().equals(vehicle.getModel().getId())) {
                throw new IllegalStateException("El repuesto " + part.getName()
                        + " no es compatible con el modelo del vehículo " + vehicle.getLicensePlate());
            }
            // RF-25: se descuenta el stock (lanza error si no alcanza)
            if (consumesStock(request.status())) {
                partService.decreaseStock(part.getId(), quantity);
            }
        }

        Repair repair = new Repair();
        repair.setDate(request.date());
        repair.setType(request.type().trim());
        repair.setStatus(request.status());
        repair.setLaborCost(request.laborCost().setScale(2, RoundingMode.HALF_UP));
        repair.setVehicle(vehicle);
        repair.setMechanic(mechanic);
        Repair saved = repository.save(repair);

        if (part != null) {
            // Se guarda el precio actual del repuesto, que queda fijo para esta reparación
            RepairPart repairPart = new RepairPart(saved, part, quantity, part.getPrice());
            saved.getRepairParts().add(repairPartRepository.save(repairPart));
        }

        return toDTO(saved);
    }

    // RF-23 y RF-25: actualizar tipo, estado y costo de mano de obra.
    // Si la reparación pasa a CANCELLED, se devuelve el stock; si sale de CANCELLED, se vuelve a descontar.
    // Pendiente (RF-05): el mecánico solo podrá modificar las reparaciones que tiene asignadas.
    public RepairDTO updateRepair(Integer repairId, RepairDTO request)
            throws EntityNotFoundException, IllegalArgumentException, IllegalStateException {
        Repair repair = findEntity(repairId);
        validateCommon(request);

        RepairStatus oldStatus = repair.getStatus();
        RepairStatus newStatus = request.status();

        if (!repair.getRepairParts().isEmpty()) {
            RepairPart repairPart = repair.getRepairParts().get(0);
            Integer partId = repairPart.getPart().getId();
            if (consumesStock(oldStatus) && !consumesStock(newStatus)) {
                partService.restoreStock(partId, repairPart.getQuantity());
            } else if (!consumesStock(oldStatus) && consumesStock(newStatus)) {
                partService.decreaseStock(partId, repairPart.getQuantity());
            }
        }

        repair.setType(request.type().trim());
        repair.setStatus(newStatus);
        repair.setLaborCost(request.laborCost().setScale(2, RoundingMode.HALF_UP));

        return toDTO(repository.save(repair));
    }

    // RF-24: eliminar reparación del historial (solo administrador).
    // Si la reparación tenía el stock descontado, se devuelve.
    public void deleteRepair(Integer repairId) throws EntityNotFoundException {
        Repair repair = findEntity(repairId);

        List<RepairPart> repairParts = new ArrayList<>(repair.getRepairParts());
        for (RepairPart repairPart : repairParts) {
            if (consumesStock(repair.getStatus())) {
                partService.restoreStock(repairPart.getPart().getId(), repairPart.getQuantity());
            }
        }
        // Primero se borran los repuestos de la reparación y luego la reparación
        repairPartRepository.deleteAll(repairParts);
        repair.getRepairParts().clear();
        repository.delete(repair);
    }

    // RF-28: registrar la ubicación de la tienda donde se compró el repuesto
    public RepairDTO saveLocation(Integer repairId, String workshopName, Double latitude, Double longitude)
            throws EntityNotFoundException, IllegalArgumentException {
        Repair repair = findEntity(repairId);

        if (workshopName == null || workshopName.isBlank()) {
            throw new IllegalArgumentException("El nombre de la tienda es obligatorio");
        }
        if (workshopName.trim().length() > 100) {
            throw new IllegalArgumentException("El nombre de la tienda no puede superar 100 caracteres");
        }
        if (latitude == null || longitude == null) {
            throw new IllegalArgumentException("Debe marcar un punto en el mapa");
        }
        if (latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException("La latitud debe estar entre -90 y 90");
        }
        if (longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("La longitud debe estar entre -180 y 180");
        }

        repair.setWorkshopName(workshopName.trim());
        repair.setWorkshopLatitude(latitude);
        repair.setWorkshopLongitude(longitude);
        return toDTO(repository.save(repair));
    }

    // RF-28: todos los puntos marcados, para dibujarlos en el mapa
    @Transactional(readOnly = true)
    public List<LocationDTO> findLocations() {
        return repository.findByWorkshopLatitudeIsNotNullAndWorkshopLongitudeIsNotNull().stream()
                .map(r -> new LocationDTO(r.getId(), r.getVehicle().getLicensePlate(), r.getType(),
                        r.getStatus(), r.getWorkshopName(), r.getWorkshopLatitude(), r.getWorkshopLongitude()))
                .toList();
    }

    private Repair findEntity(Integer repairId) {
        return repository.findById(repairId)
                .orElseThrow(() -> new EntityNotFoundException("Reparación con id " + repairId + " no encontrada"));
    }

    // Una reparación cancelada no consume stock; las demás sí
    private boolean consumesStock(RepairStatus status) {
        return status != RepairStatus.CANCELLED;
    }

    private RepairDTO toDTO(Repair repair) {
        RepairPart repairPart = repair.getRepairParts().isEmpty() ? null : repair.getRepairParts().get(0);

        // Costo total = mano de obra + (precio unitario del repuesto x cantidad)
        BigDecimal totalCost = repair.getLaborCost();
        if (repairPart != null) {
            totalCost = totalCost.add(repairPart.getUnitPrice()
                    .multiply(BigDecimal.valueOf(repairPart.getQuantity())));
        }

        return new RepairDTO(
                repair.getId(),
                repair.getDate(),
                repair.getType(),
                repair.getStatus(),
                repair.getLaborCost(),
                repair.getVehicle().getId(),
                repair.getVehicle().getLicensePlate(),
                repair.getMechanic().getId(),
                repair.getMechanic().getName(),
                repairPart == null ? null : repairPart.getPart().getId(),
                repairPart == null ? null : repairPart.getPart().getName(),
                repairPart == null ? null : repairPart.getQuantity(),
                repairPart == null ? null : repairPart.getUnitPrice(),
                totalCost,
                repair.getWorkshopName(),
                repair.getWorkshopLatitude(),
                repair.getWorkshopLongitude());
    }

    // Validaciones que comparten el registro y la actualización
    private void validateCommon(RepairDTO request) {
        if (request.type() == null || request.type().isBlank()) {
            throw new IllegalArgumentException("El tipo de reparación es obligatorio");
        }
        if (request.type().trim().length() > 100) {
            throw new IllegalArgumentException("El tipo de reparación no puede superar 100 caracteres");
        }
        if (request.status() == null) {
            throw new IllegalArgumentException("El estado es obligatorio");
        }
        if (request.laborCost() == null) {
            throw new IllegalArgumentException("El costo de mano de obra es obligatorio");
        }
        if (request.laborCost().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El costo de mano de obra no puede ser negativo");
        }
        if (request.laborCost().compareTo(MAX_LABOR_COST) > 0) {
            throw new IllegalArgumentException("El costo de mano de obra no puede superar 99.999.999,99");
        }
    }
}