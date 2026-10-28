package dev.tarazonarodriguezrojas.taller1.model.dto;

/**
 * Resultado de buscar el modelo y la marca de un vehículo por su placa (RF-27).
 * plateType describe el tipo de placa según su formato (carro, moto, remolque, etc.).
 */
public record PlateModelDTO(
        String licensePlate,
        String plateType,
        String modelName,
        String brand)
{}
