package dev.tarazonarodriguezrojas.taller1.model.dto;

import java.math.BigDecimal;

/**
 * Datos de un repuesto que viajan entre el formulario, el controller y el service.
 *
 * Al registrar o actualizar, el formulario envía: name, description, price, ean,
 * stock y modelId. Al consultar, el service además llena imageUrl (nombre del
 * archivo de la imagen), modelName y modelBrand para mostrarlos.
 */
public record PartDTO(
        Integer id,
        String name,
        String description,
        BigDecimal price,
        String ean,
        Integer stock,
        String imageUrl,
        Integer modelId,
        String modelName,
        String modelBrand)
{}
