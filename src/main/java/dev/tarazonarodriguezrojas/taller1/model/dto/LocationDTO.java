package dev.tarazonarodriguezrojas.taller1.model.dto;

import dev.tarazonarodriguezrojas.taller1.model.constant.RepairStatus;

/**
 * Un punto del mapa (RF-28): lugar donde se realizó una reparación.
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