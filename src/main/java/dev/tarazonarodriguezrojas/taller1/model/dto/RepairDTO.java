package dev.tarazonarodriguezrojas.taller1.model.dto;

import dev.tarazonarodriguezrojas.taller1.model.constant.RepairStatus;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Datos de una reparación que viajan entre el formulario, el controller y el service.
 *
 * Al registrar, el formulario envía: date, type, status, laborCost, vehicleId,
 * mechanicId y, opcionalmente, partId y quantity.
 * Al consultar, el service además llena los nombres, el precio unitario del repuesto,
 * totalCost (mano de obra + repuesto) y la ubicación de la tienda.
 */
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
        Integer partId,
        String partName,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal totalCost,
        String workshopName,
        Double workshopLatitude,
        Double workshopLongitude)
{}