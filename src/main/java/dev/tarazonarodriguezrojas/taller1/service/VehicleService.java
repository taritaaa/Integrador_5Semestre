package dev.tarazonarodriguezrojas.taller1.service;

import dev.tarazonarodriguezrojas.taller1.model.constant.PlateColor;
import dev.tarazonarodriguezrojas.taller1.model.constant.PlateRules;
import dev.tarazonarodriguezrojas.taller1.model.constant.ServiceType;
import dev.tarazonarodriguezrojas.taller1.model.dto.PlateModelDTO;
import dev.tarazonarodriguezrojas.taller1.model.dto.VehicleDTO;
import dev.tarazonarodriguezrojas.taller1.model.entity.Vehicle;
import dev.tarazonarodriguezrojas.taller1.model.entity.VehicleModel;
import dev.tarazonarodriguezrojas.taller1.repository.VehicleModelRepository;
import dev.tarazonarodriguezrojas.taller1.repository.VehicleRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.regex.Pattern;

@Service
@Transactional
public class VehicleService {

    // Formatos de placa colombiana aceptados (los del diagrama de base de datos):
    //  AAA-123           carros, camionetas, camiones, buses y tractocamiones (tractomulas)
    //  AAA-12A, AAA-12   motos
    //  R-12345           remolques
    //  S-12345           semirremolques (el tráiler de una tractomula)
    //  CC-1234, CD-1234  diplomáticos
    // Este filtro revisa que la placa tenga algún formato válido; después se revisa que sea
    // el que le corresponde al tipo de vehículo del modelo y al servicio del vehículo
    // (validatePlateForModel).
    private static final Pattern PLATE_PATTERN = Pattern.compile(
            "^(?:[A-Z]{3}-\\d{3}|[A-Z]{3}-\\d{2}[A-Z]?|[RS]-\\d{5}|C[CD]-\\d{4})$");

    // Mensaje que se muestra cuando la placa no tiene un formato válido
    private static final String PLATE_FORMATS_HELP =
            "Formatos válidos: ABC-123 (carro, camioneta, camión o tractomula), ABC-12 o ABC-12A (moto), "
                    + "R-12345 (remolque), S-12345 (semirremolque), CC-1234 o CD-1234 (diplomático)";

    private final VehicleRepository repository;
    private final VehicleModelRepository modelRepository;

    public VehicleService(VehicleRepository repository, VehicleModelRepository modelRepository) {
        this.repository = repository;
        this.modelRepository = modelRepository;
    }

    // RF-13: listado, con filtros opcionales por placa y propietario
    @Transactional(readOnly = true)
    public List<VehicleDTO> findAllVehicles(String plate, String owner) {
        // El filtro de placa se limpia: mayúsculas y sin guiones ni espacios
        String plateFilter = plate == null ? "" : plate.trim().toUpperCase().replace("-", "").replace(" ", "");
        String ownerFilter = owner == null ? "" : owner.trim();
        return repository.searchVehicles(plateFilter, ownerFilter)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    // RF-13: detalle de un vehículo
    @Transactional(readOnly = true)
    public VehicleDTO findVehicleById(Integer vehicleId) throws EntityNotFoundException {
        return toDTO(findEntity(vehicleId));
    }

    // RF-12: registrar vehículo
    public VehicleDTO createVehicle(VehicleDTO request)
            throws EntityNotFoundException, IllegalArgumentException {
        validate(request);

        String plate = normalizePlate(request.licensePlate());
        if (!isValidLicensePlate(plate)) {
            throw new IllegalArgumentException("Placa no válida: " + plate + ". " + PLATE_FORMATS_HELP);
        }
        if (repository.existsByLicensePlate(plate)) {
            throw new IllegalArgumentException("Ya existe un vehículo con la placa " + plate);
        }

        VehicleModel model = findModel(request.modelId());
        // La placa debe corresponder al tipo de vehículo del modelo y al servicio elegido
        validatePlateForModel(plate, model, request.serviceType());

        Vehicle vehicle = new Vehicle();
        vehicle.setLicensePlate(plate);
        setAttributesFromDTO(request, vehicle, model);

        return toDTO(repository.save(vehicle));
    }

    // RF-14: actualizar vehículo. La placa NO se modifica (identifica al vehículo).
    public VehicleDTO updateVehicle(Integer vehicleId, VehicleDTO request)
            throws EntityNotFoundException, IllegalArgumentException {
        Vehicle vehicle = findEntity(vehicleId);
        validate(request);

        VehicleModel model = findModel(request.modelId());
        // Si cambia el modelo o el servicio, la placa que ya tiene debe seguir siendo válida
        validatePlateForModel(vehicle.getLicensePlate(), model, request.serviceType());
        setAttributesFromDTO(request, vehicle, model);

        return toDTO(repository.save(vehicle));
    }

    // RF-15: eliminar vehículo. Si tiene reparaciones asociadas, no se elimina.
    public void deleteVehicle(Integer vehicleId) throws EntityNotFoundException, IllegalStateException {
        Vehicle vehicle = findEntity(vehicleId);
        if (!vehicle.getRepairs().isEmpty()) {
            throw new IllegalStateException("No se puede eliminar el vehículo " + vehicle.getLicensePlate()
                    + " porque tiene reparaciones asociadas");
        }
        repository.delete(vehicle);
    }

    // RF-27: buscar el modelo y la marca de un vehículo por su placa (cualquier tipo de placa)
    @Transactional(readOnly = true)
    public PlateModelDTO findModelByPlate(String licensePlate)
            throws EntityNotFoundException, IllegalArgumentException {
        String plate = normalizePlate(licensePlate);
        if (!isValidLicensePlate(plate)) {
            throw new IllegalArgumentException("Placa no válida: " + plate + ". " + PLATE_FORMATS_HELP);
        }
        Vehicle vehicle = repository.findByLicensePlate(plate)
                .orElseThrow(() -> new EntityNotFoundException(
                        "No hay un vehículo registrado con la placa " + plate));
        VehicleModel model = vehicle.getModel();
        // El tipo sale del modelo (carro, camioneta, tractomula...) y el servicio, del vehículo
        return new PlateModelDTO(vehicle.getLicensePlate(),
                model.getVehicleType().label() + " - " + vehicle.getServiceType().label(),
                model.getName(),
                model.getBrand());
    }

    private Vehicle findEntity(Integer vehicleId) {
        return repository.findById(vehicleId)
                .orElseThrow(() -> new EntityNotFoundException("Vehículo con id " + vehicleId + " no encontrado"));
    }

    private VehicleModel findModel(Integer modelId) {
        if (modelId == null) {
            throw new IllegalArgumentException("Debe seleccionar un modelo");
        }
        return modelRepository.findById(modelId)
                .orElseThrow(() -> new EntityNotFoundException("Modelo con id " + modelId + " no encontrado"));
    }

    // La placa (ya normalizada) debe tener el formato del tipo de vehículo del modelo y del
    // servicio elegido. Ejemplo: un modelo de moto solo acepta placas como ABC-12A, y un
    // vehículo diplomático solo acepta CC-1234 o CD-1234.
    private void validatePlateForModel(String plate, VehicleModel model, ServiceType service) {
        if (!PlateRules.matches(model.getVehicleType(), service, plate)) {
            throw new IllegalArgumentException("La placa " + plate + " no corresponde al tipo de vehículo ("
                    + model.getVehicleType().label() + ") con servicio " + service.label() + ". "
                    + "Formato esperado: " + PlateRules.format(model.getVehicleType(), service));
        }
    }

    // El color sale del servicio y del tipo de vehículo. Solo en los vehículos oficiales,
    // que no tienen un color fijo, se usa el que eligió el usuario.
    private PlateColor resolvePlateColor(VehicleDTO request, VehicleModel model) {
        PlateColor color = PlateRules.plateColor(model.getVehicleType(), request.serviceType());
        if (color != null) {
            return color;
        }
        if (request.plateColor() == null) {
            throw new IllegalArgumentException("Para un vehículo oficial debe elegir el color de la placa");
        }
        return request.plateColor();
    }

    // Copia los datos del DTO a la entidad (todos menos la placa, que se asigna solo al crear)
    private void setAttributesFromDTO(VehicleDTO request, Vehicle vehicle, VehicleModel model) {
        // Se calcula primero: si falla, todavía no se modificó nada en la entidad
        PlateColor plateColor = resolvePlateColor(request, model);
        vehicle.setServiceType(request.serviceType());
        vehicle.setPlateColor(plateColor);
        vehicle.setOwnerName(request.ownerName().trim());
        vehicle.setOwnerDocument(request.ownerDocument().trim());
        vehicle.setOwnerPhone(request.ownerPhone().trim());
        vehicle.setModel(model);
    }

    private VehicleDTO toDTO(Vehicle vehicle) {
        return new VehicleDTO(
                vehicle.getId(),
                vehicle.getLicensePlate(),
                vehicle.getServiceType(),
                vehicle.getPlateColor(),
                vehicle.getOwnerName(),
                vehicle.getOwnerDocument(),
                vehicle.getOwnerPhone(),
                vehicle.getModel().getId(),
                vehicle.getModel().getName(),
                vehicle.getModel().getBrand());
    }

    /**
     * Deja la placa en el formato estándar: mayúsculas, sin espacios y con guion.
     * Ejemplos: "abc123" -> "ABC-123", "abc12d" -> "ABC-12D", "s12345" -> "S-12345".
     * Si ya trae guion, solo se pasa a mayúsculas.
     */
    private String normalizePlate(String plate) {
        if (plate == null) {
            return "";
        }
        String clean = plate.trim().toUpperCase().replace(" ", "");
        if (clean.contains("-")) {
            return clean;
        }
        // Carro, camión o tractomula (ABC123) y moto (ABC12 o ABC12A): el guion va después de 3 letras
        if (clean.matches("[A-Z]{3}\\d{3}") || clean.matches("[A-Z]{3}\\d{2}[A-Z]?")) {
            return clean.substring(0, 3) + "-" + clean.substring(3);
        }
        // Remolque (R12345) y semirremolque (S12345): el guion va después de la primera letra
        if (clean.matches("[RS]\\d{5}")) {
            return clean.substring(0, 1) + "-" + clean.substring(1);
        }
        // Diplomático (CC1234 o CD1234): el guion va después de las 2 letras
        if (clean.matches("C[CD]\\d{4}")) {
            return clean.substring(0, 2) + "-" + clean.substring(2);
        }
        return clean;
    }

    private boolean isValidLicensePlate(String plate) {
        return PLATE_PATTERN.matcher(plate).matches();
    }

    // Validaciones de los datos que no dependen de la base de datos
    private void validate(VehicleDTO request) {
        if (request.serviceType() == null) {
            throw new IllegalArgumentException("El tipo de servicio es obligatorio");
        }
        if (request.ownerName() == null || request.ownerName().isBlank()) {
            throw new IllegalArgumentException("El nombre del propietario es obligatorio");
        }
        if (request.ownerName().trim().length() > 100) {
            throw new IllegalArgumentException("El nombre del propietario no puede superar 100 caracteres");
        }
        if (request.ownerDocument() == null || request.ownerDocument().isBlank()) {
            throw new IllegalArgumentException("El documento del propietario es obligatorio");
        }
        if (request.ownerDocument().trim().length() > 20) {
            throw new IllegalArgumentException("El documento no puede superar 20 caracteres");
        }
        if (request.ownerPhone() == null || request.ownerPhone().isBlank()) {
            throw new IllegalArgumentException("El teléfono del propietario es obligatorio");
        }
        if (request.ownerPhone().trim().length() > 20) {
            throw new IllegalArgumentException("El teléfono no puede superar 20 caracteres");
        }
    }
}