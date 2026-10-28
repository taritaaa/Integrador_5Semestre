package dev.tarazonarodriguezrojas.taller1.model.dto;

import dev.tarazonarodriguezrojas.taller1.model.constant.PlateColor;

/**
 * Datos de un vehículo que viajan entre el formulario, el controller y el service.
 *
 * Al registrar o actualizar, el formulario envía: licensePlate, plateColor,
 * ownerName, ownerDocument, ownerPhone y modelId.
 * Al consultar, el service además llena modelName y modelBrand para mostrarlos.
 */
public record VehicleDTO(
        Integer id,
        String licensePlate,
        PlateColor plateColor,
        String ownerName,
        String ownerDocument,
        String ownerPhone,
        Integer modelId,
        String modelName,
        String modelBrand)
{}