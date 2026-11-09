package dev.tarazonarodriguezrojas.taller1.model.dto;

import dev.tarazonarodriguezrojas.taller1.model.constant.RepairStatus;
import org.springframework.format.annotation.DateTimeFormat;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record RepairDTO(
        Integer id,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
        String type,
        RepairStatus status,
        BigDecimal laborCost,
        Integer vehicleId,
        String licensePlate,
        Integer mechanicId,
        String mechanicName,
        List<RepairItemDTO> items,
        BigDecimal totalCost,
        String workshopName,
        Double workshopLatitude,
        Double workshopLongitude) {
}
