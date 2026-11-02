package dev.tarazonarodriguezrojas.taller1.model.dto;

import dev.tarazonarodriguezrojas.taller1.model.constant.PlateColor;
import dev.tarazonarodriguezrojas.taller1.model.constant.PlateRules;
import dev.tarazonarodriguezrojas.taller1.model.constant.ServiceType;
import dev.tarazonarodriguezrojas.taller1.model.constant.VehicleType;

/**
 * Datos de un modelo de vehículo que viajan entre el formulario, el controller y el service
 * (RF-08, RF-09, RF-10). El id es nulo cuando se está registrando un modelo nuevo.
 *
 * Los métodos de abajo no son datos del formulario: calculan textos para las plantillas
 * (nombre en español del tipo y formato de placa que le corresponde al modelo).
 */
public record VehicleModelDTO(
        Integer id,
        String name,
        String brand,
        VehicleType vehicleType,
        ServiceType serviceType)
{
    public String vehicleTypeLabel() {
        return vehicleType == null ? "" : vehicleType.label();
    }

    public String serviceTypeLabel() {
        return serviceType == null ? "" : serviceType.label();
    }

    // Ejemplo de placa para este modelo (vacío si aún no tiene tipo y servicio)
    public String plateExample() {
        return hasPlateRules() ? PlateRules.example(vehicleType, serviceType) : "";
    }

    // Formato de placa para este modelo, en texto
    public String plateFormat() {
        return hasPlateRules() ? PlateRules.format(vehicleType, serviceType) : "";
    }

    // Color de placa sugerido (nulo para oficial y diplomático)
    public PlateColor suggestedPlateColor() {
        return serviceType == null ? null : serviceType.suggestedPlateColor();
    }

    private boolean hasPlateRules() {
        return vehicleType != null && serviceType != null;
    }
}