package dev.tarazonarodriguezrojas.taller1.model.dto;

import dev.tarazonarodriguezrojas.taller1.model.constant.Role;

/**
 * Datos de un usuario que viajan entre el formulario, el controller y el service.
 *
 * Al registrar (RF-06) el formulario envía: name, document, email, phone, password y role.
 * Al actualizar (RF-07) solo se modifican: name, phone, role y active.
 * La contraseña solo viaja al registrar; al consultar siempre es nula.
 */
public record UserDTO(
        Integer id,
        String name,
        String document,
        String email,
        String phone,
        String password,
        Role role,
        Boolean active)
{}