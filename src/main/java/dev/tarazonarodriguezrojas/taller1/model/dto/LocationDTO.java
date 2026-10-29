package dev.tarazonarodriguezrojas.taller1.model.dto;

import dev.tarazonarodriguezrojas.taller1.model.constant.RepairStatus;

/**
 * Un punto del mapa de tiendas (RF-28): dónde se compró el repuesto de una reparación.
 */
public record LocationDTO(
        Integer repairId,
        String licensePlate,
        String type,
        RepairStatus status,
        String workshopName,
        Double latitude,
        Double longitude)
{}