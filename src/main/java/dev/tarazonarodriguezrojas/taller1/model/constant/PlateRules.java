package dev.tarazonarodriguezrojas.taller1.model.constant;

/**
 * Reglas de la placa según el tipo de vehículo y el tipo de servicio del modelo.
 * Un vehículo diplomático usa siempre el formato CC-1234 o CD-1234; los demás usan el de su tipo.
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

    private static String regex(VehicleType type, ServiceType service) {
        return service == ServiceType.DIPLOMATIC ? DIPLOMATIC_REGEX : type.plateRegex();
    }
}