package dev.tarazonarodriguezrojas.taller1.model.dto;

import java.math.BigDecimal;

public record RepairItemDTO(Integer partId, String partName, Integer quantity,
                            BigDecimal unitPrice, BigDecimal subtotal) {
}
