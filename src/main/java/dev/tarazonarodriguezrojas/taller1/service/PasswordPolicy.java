package dev.tarazonarodriguezrojas.taller1.service;

/**
 * Reglas que debe cumplir una contraseña nueva.
 * Se usa al registrar un usuario (RF-06) y al restablecer la contraseña (RF-04).
 */
public final class PasswordPolicy {

    private static final int MIN_LENGTH = 8;
    // BCrypt solo usa los primeros 72 caracteres
    private static final int MAX_LENGTH = 72;

    private PasswordPolicy() {
    }

    public static void validate(String password) throws IllegalArgumentException {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("La contraseña es obligatoria");
        }
        if (password.length() < MIN_LENGTH) {
            throw new IllegalArgumentException("La contraseña debe tener al menos " + MIN_LENGTH + " caracteres");
        }
        if (password.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("La contraseña no puede superar " + MAX_LENGTH + " caracteres");
        }
    }
}