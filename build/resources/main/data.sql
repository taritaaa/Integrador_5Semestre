-- Usuarios de prueba, solo para poder registrar reparaciones mientras no exista el módulo de usuarios.
-- La contraseña "pendiente" NO es una contraseña real: se reemplazará con contraseñas cifradas
-- cuando se implemente el inicio de sesión (RF-01).
insert into users (name, document, email, phone, password_hash, role, is_active)
values ('Administrador de prueba', '1000000001', 'admin@taller.test', '3000000001', 'pendiente', 'ADMIN', true),
       ('Mecánico de prueba 1', '1000000002', 'mecanico1@taller.test', '3000000002', 'pendiente', 'MECHANIC', true),
       ('Mecánico de prueba 2', '1000000003', 'mecanico2@taller.test', '3000000003', 'pendiente', 'MECHANIC', true);