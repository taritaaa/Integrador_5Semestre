package dev.tarazonarodriguezrojas.taller1.service;

import dev.tarazonarodriguezrojas.taller1.model.dto.VehicleModelDTO;
import dev.tarazonarodriguezrojas.taller1.model.entity.VehicleModel;
import dev.tarazonarodriguezrojas.taller1.repository.VehicleModelRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class VehicleModelService {

    private final VehicleModelRepository repository;

    public VehicleModelService(VehicleModelRepository repository) {
        this.repository = repository;
    }

    // RF-09: listado de modelos
    public List<VehicleModelDTO> findAllModels() {
        return repository.findAll().stream().map(this::toDTO).toList();
    }

    // RF-09: detalle de un modelo
    public VehicleModelDTO findModelById(Integer modelId) throws EntityNotFoundException {
        VehicleModel model = findEntity(modelId);
        return toDTO(model);
    }

    // RF-08: registrar modelo
    public VehicleModelDTO createModel(VehicleModelDTO request) throws IllegalArgumentException {
        validate(request);
        String name = request.name().trim();
        String brand = request.brand().trim();
        if (repository.existsByNameIgnoreCaseAndBrandIgnoreCase(name, brand)) {
            throw new IllegalArgumentException("El modelo " + name + " de la marca " + brand + " ya existe");
        }
        VehicleModel model = new VehicleModel();
        setAttributesFromDTO(request, model);
        return toDTO(repository.save(model));
    }

    // RF-10: actualizar modelo
    public VehicleModelDTO updateModel(Integer modelId, VehicleModelDTO request)
            throws EntityNotFoundException, IllegalArgumentException {
        validate(request);
        VehicleModel model = findEntity(modelId);
        String name = request.name().trim();
        String brand = request.brand().trim();
        if (repository.existsByNameIgnoreCaseAndBrandIgnoreCaseAndIdNot(name, brand, modelId)) {
            throw new IllegalArgumentException("El modelo " + name + " de la marca " + brand + " ya existe");
        }
        setAttributesFromDTO(request, model);
        return toDTO(repository.save(model));
    }

    // RF-11: eliminar modelo. Si tiene vehículos o repuestos asociados, no se elimina
    // y se informa el motivo. @Transactional permite leer las listas del modelo.
    @Transactional
    public void deleteModel(Integer modelId) throws EntityNotFoundException, IllegalStateException {
        VehicleModel model = findEntity(modelId);
        if (!model.getVehicles().isEmpty()) {
            throw new IllegalStateException("No se puede eliminar el modelo " + model.getName()
                    + " porque tiene vehículos asociados");
        }
        if (!model.getParts().isEmpty()) {
            throw new IllegalStateException("No se puede eliminar el modelo " + model.getName()
                    + " porque tiene repuestos asociados");
        }
        repository.delete(model);
    }

    private VehicleModel findEntity(Integer modelId) {
        return repository.findById(modelId)
                .orElseThrow(() -> new EntityNotFoundException("Modelo con id " + modelId + " no encontrado"));
    }

    private void setAttributesFromDTO(VehicleModelDTO request, VehicleModel model) {
        model.setName(request.name().trim());
        model.setBrand(request.brand().trim());
    }

    private VehicleModelDTO toDTO(VehicleModel model) {
        return new VehicleModelDTO(model.getId(), model.getName(), model.getBrand());
    }

    // Nombre y marca son obligatorios y miden máximo 100 caracteres (igual que la columna)
    private void validate(VehicleModelDTO request) {
        if (request.name() == null || request.name().isBlank()) {
            throw new IllegalArgumentException("El nombre del modelo es obligatorio");
        }
        if (request.brand() == null || request.brand().isBlank()) {
            throw new IllegalArgumentException("La marca del modelo es obligatoria");
        }
        if (request.name().trim().length() > 100) {
            throw new IllegalArgumentException("El nombre no puede superar 100 caracteres");
        }
        if (request.brand().trim().length() > 100) {
            throw new IllegalArgumentException("La marca no puede superar 100 caracteres");
        }
    }
}