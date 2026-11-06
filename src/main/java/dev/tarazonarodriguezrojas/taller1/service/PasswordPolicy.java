package dev.tarazonarodriguezrojas.taller1.service;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Reglas que debe cumplir una contraseña nueva.
 * Se usa al cambiar la contraseña temporal, al restablecerla por correo (RF-04)
 * y para generar la contraseña temporal de un usuario nuevo (RF-06).
 *
 * Una contraseña de ALTA seguridad tiene entre 8 y 72 caracteres, con al menos
 * una mayúscula, una minúscula, un número y un símbolo.
 */
public final class PasswordPolicy {

    private static final int MIN_LENGTH = 8;
    // BCrypt solo usa los primeros 72 caracteres
    private static final int MAX_LENGTH = 72;
    private static final int TEMP_LENGTH = 12;

    // Para la contraseña temporal se omiten caracteres que se confunden (O/0, l/1/I)
    private static final String UPPER = "ABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final String LOWER = "abcdefghijkmnopqrstuvwxyz";
    private static final String DIGITS = "23456789";
    private static final String SYMBOLS = "!@#$%&*?";
    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordPolicy() {
    }

    // Lanza IllegalArgumentException indicando exactamente qué le falta a la contraseña
    public static void validate(String password) throws IllegalArgumentException {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("La contraseña es obligatoria");
        }
        if (password.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("La contraseña no puede superar " + MAX_LENGTH + " caracteres");
        }

        boolean upper = false;
        boolean lower = false;
        boolean digit = false;
        boolean symbol = false;
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

        List<String> missing = new ArrayList<>();
        if (password.length() < MIN_LENGTH) {
            missing.add("mínimo " + MIN_LENGTH + " caracteres");
        }
        if (!upper) {
            missing.add("una mayúscula");
        }
        if (!lower) {
            missing.add("una minúscula");
        }
        if (!digit) {
            missing.add("un número");
        }
        if (!symbol) {
            missing.add("un símbolo (ej: !@#$%&*)");
        }

        if (!missing.isEmpty()) {
            throw new IllegalArgumentException(
                    "Contraseña de baja seguridad. Le falta: " + String.join(", ", missing));
        }
    }

    // Contraseña temporal de 12 caracteres que ya cumple todas las reglas
    public static String generateTemporary() {
        List<Character> chars = new ArrayList<>();
        chars.add(pick(UPPER));
        chars.add(pick(LOWER));
        chars.add(pick(DIGITS));
        chars.add(pick(SYMBOLS));
        String all = UPPER + LOWER + DIGITS + SYMBOLS;
        while (chars.size() < TEMP_LENGTH) {
            chars.add(pick(all));
        }
        Collections.shuffle(chars, RANDOM);
        StringBuilder sb = new StringBuilder();
        for (char c : chars) {
            sb.append(c);
        }
        return sb.toString();
    }

    private static char pick(String source) {
        return source.charAt(RANDOM.nextInt(source.length()));
    }
}
