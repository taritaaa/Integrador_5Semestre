package dev.tarazonarodriguezrojas.taller1.model.dto;

import dev.tarazonarodriguezrojas.taller1.model.constant.VehicleType;

/**
 * Datos de un modelo de vehículo que viajan entre el formulario, el controller y el service
 * (RF-08, RF-09, RF-10). El id es nulo cuando se está registrando un modelo nuevo.
 *
 * Los métodos de abajo no son datos del formulario: calculan textos para las plantillas
 * (nombre en español del tipo y formato de placa que le corresponde al modelo).
 * El servicio (particular, público...) y el color de la placa ya no dependen del modelo:
 * se piden al registrar el vehículo.
 */
public record VehicleModelDTO(
        Integer id,
        String name,
        String brand,
        VehicleType vehicleType)
{
    public String vehicleTypeLabel() {
        return vehicleType == null ? "" : vehicleType.label();
    }

    // Ejemplo de placa para este modelo (vacío si aún no tiene tipo)
    public String plateExample() {
        return vehicleType == null ? "" : vehicleType.plateExample();
    }

    // Formato de placa para este modelo, en texto (los diplomáticos usan otro: ver PlateRules)
    public String plateFormat() {
        return vehicleType == null ? "" : vehicleType.plateExample() + " (" + vehicleType.plateFormat() + ")";
    }

    // ¿Es remolque o semirremolque? Estos llevan siempre placa verde
    public boolean trailerType() {
        return vehicleType != null && vehicleType.isTrailer();
    }
}