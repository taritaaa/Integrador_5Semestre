package dev.tarazonarodriguezrojas.taller1.model.dto;

import dev.tarazonarodriguezrojas.taller1.model.constant.PlateColor;
import dev.tarazonarodriguezrojas.taller1.model.constant.ServiceType;

/**
 * Datos de un vehículo que viajan entre el formulario, el controller y el service.
 *
 * Al registrar o actualizar, el formulario envía: licensePlate, serviceType,
 * ownerName, ownerDocument, ownerPhone y modelId. El color de la placa (plateColor) solo se
 * envía en vehículos oficiales; en los demás lo calcula el service.
 * Al consultar, el service además llena plateColor, modelName y modelBrand para mostrarlos.
 */
public record VehicleDTO(
        Integer id,
        String licensePlate,
        ServiceType serviceType,
        PlateColor plateColor,
        String ownerName,
        String ownerDocument,
        String ownerPhone,
        Integer modelId,
        String modelName,
        String modelBrand)
{
    public String serviceTypeLabel() {
        return serviceType == null ? "" : serviceType.label();
    }

    public String plateColorLabel() {
        return plateColor == null ? "" : plateColor.label();
    }
}