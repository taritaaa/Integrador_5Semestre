package dev.tarazonarodriguezrojas.taller1.model.dto;

/**
 * Datos de un modelo de vehículo que viajan entre el formulario,
 * el controller y el service (RF-08, RF-09, RF-10).
 * El id es nulo cuando se está registrando un modelo nuevo.
 */
public record VehicleModelDTO(
        Integer id,
        String name,
        String brand)
{}