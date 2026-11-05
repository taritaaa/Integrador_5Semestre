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
        boolean upper = false, lower = false, digit = false, symbol = false;
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) {
                upper = true;
            } else if (Character.isLowerCase(c)) {
                lower = true;
            } else if (Character.isDigit(c)) {
                digit = true;
            } else if (!Character.isWhitespace(c)) {
                symbol = true;
            }
        }

        if (!upper) {
            throw new IllegalArgumentException("La contraseña debe tener al menos una letra mayúscula");
        }
        if (!lower) {
            throw new IllegalArgumentException("La contraseña debe tener al menos una letra minúscula");
        }
        if (!digit) {
            throw new IllegalArgumentException("La contraseña debe tener al menos un número");
        }
        if (!symbol) {
            throw new IllegalArgumentException("La contraseña debe tener al menos un símbolo (ej: !@#$%&*)");
        }
    }

}