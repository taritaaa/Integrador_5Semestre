package dev.tarazonarodriguezrojas.taller1.model.constant;


/**
 * Reglas de la placa según el tipo de vehículo (del modelo) y el tipo de servicio (del vehículo).
 *
 * Formato: un vehículo diplomático usa siempre CC-1234 o CD-1234; los demás usan el de su tipo.
 *
 * Color (se calcula, el usuario no lo elige salvo en vehículos oficiales):
 *   diplomático                 -> azul
 *   remolque y semirremolque    -> verde
 *   particular                  -> amarilla
 *   público                     -> blanca
 *   oficial                     -> sin color fijo (lo elige el usuario)
 */
public final class PlateRules {

    private static final String DIPLOMATIC_REGEX = "C[CD]-\\d{4}";
    private static final String DIPLOMATIC_EXAMPLE = "CD-1234";

    private PlateRules() {
    }

    // ¿La placa (ya normalizada, con guion) corresponde a ese tipo y servicio?
    public static boolean matches(VehicleType type, ServiceType service, String plate) {
        return plate != null && plate.matches(regex(type, service));
    }

    public static String example(VehicleType type, ServiceType service) {
        return service == ServiceType.DIPLOMATIC ? DIPLOMATIC_EXAMPLE : type.plateExample();
    }

    // Texto para mostrar al usuario, por ejemplo: "ABC-123 (3 letras, guion y 3 números)"
    public static String format(VehicleType type, ServiceType service) {
        if (service == ServiceType.DIPLOMATIC) {
            return "CC-1234 o CD-1234 (2 letras, guion y 4 números)";
        }
        return type.plateExample() + " (" + type.plateFormat() + ")";
    }

    // Color que le corresponde a la placa. Devuelve null si no hay un color fijo (oficial):
    // en ese caso lo elige el usuario.
    public static PlateColor plateColor(VehicleType type, ServiceType service) {
        if (service == ServiceType.DIPLOMATIC) {
            return PlateColor.BLUE;
        }
        if (type.isTrailer()) {
            return PlateColor.GREEN;
        }
        return switch (service) {
            case PRIVATE -> PlateColor.YELLOW;
            case PUBLIC -> PlateColor.WHITE;
            case OFFICIAL, DIPLOMATIC -> null;
        };
    }

    private static String regex(VehicleType type, ServiceType service) {
        return service == ServiceType.DIPLOMATIC ? DIPLOMATIC_REGEX : type.plateRegex();
    }
}