package dev.tarazonarodriguezrojas.taller1.service;

public class PhoneValidation {

    // Se guarda el indicativo junto con el numero, sin espacios (formato internacional).
    // Esta validacion comprueba formato, no que la linea exista o este activa.
    public static String validate(String phone) {
        if (phone == null || phone.isBlank()) {
            throw new IllegalArgumentException("El numero de celular es obligatorio");
        }
        String clean = phone.trim().replace(" ", "").replace("-", "");
        if (!clean.matches("\\+[1-9][0-9]{7,14}")) {
            throw new IllegalArgumentException("Ingrese el indicativo y el numero sin caracteres invalidos");
        }
        if (clean.startsWith("+57") && clean.substring(3).matches("3[0-9]{9}")) return clean; // Colombia
        if (clean.startsWith("+52") && clean.substring(3).matches("[0-9]{10}")) return clean; // Mexico
        if (clean.startsWith("+1") && clean.substring(2).matches("[2-9][0-9]{2}[2-9][0-9]{6}")) return clean; // EE.UU./Canada
        if (clean.startsWith("+34") && clean.substring(3).matches("[67][0-9]{8}")) return clean; // Espana
        if (clean.startsWith("+56") && clean.substring(3).matches("9[0-9]{8}")) return clean; // Chile
        if (clean.startsWith("+51") && clean.substring(3).matches("9[0-9]{8}")) return clean; // Peru
        throw new IllegalArgumentException("Numero no valido para el pais elegido. Se admiten Colombia, Mexico, EE.UU./Canada, Espana, Chile y Peru");
    }
}
