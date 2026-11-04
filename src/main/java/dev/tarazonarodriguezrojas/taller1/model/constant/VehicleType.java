package dev.tarazonarodriguezrojas.taller1.model.constant;

/**
 * Tipo de vehículo de un modelo. Cada tipo sabe qué formato de placa colombiana le corresponde,
 * así al registrar un vehículo se le indica al usuario cómo escribir la placa.
 */
public enum VehicleType {

    CAR("Carro", "[A-Z]{3}-\\d{3}", "ABC-123", "3 letras, guion y 3 números"),
    SUV("Camioneta", "[A-Z]{3}-\\d{3}", "ABC-123", "3 letras, guion y 3 números"),
    TRUCK("Camión", "[A-Z]{3}-\\d{3}", "ABC-123", "3 letras, guion y 3 números"),
    TRACTOR_TRUCK("Tractocamión (tractomula)", "[A-Z]{3}-\\d{3}", "ABC-123", "3 letras, guion y 3 números"),
    BUS("Bus o buseta", "[A-Z]{3}-\\d{3}", "ABC-123", "3 letras, guion y 3 números"),
    MOTORCYCLE("Moto", "[A-Z]{3}-\\d{2}[A-Z]?", "ABC-12A", "3 letras, guion, 2 números y, opcionalmente, 1 letra"),
    TRAILER("Remolque", "R-\\d{5}", "R-12345", "la letra R, guion y 5 números"),
    SEMITRAILER("Semirremolque (tráiler)", "S-\\d{5}", "S-12345", "la letra S, guion y 5 números");

    private final String label;
    private final String plateRegex;
    private final String plateExample;
    private final String plateFormat;

    VehicleType(String label, String plateRegex, String plateExample, String plateFormat) {
        this.label = label;
        this.plateRegex = plateRegex;
        this.plateExample = plateExample;
        this.plateFormat = plateFormat;
    }

    // Nombre en español para mostrar en pantalla
    public String label() {
        return label;
    }

    public String plateRegex() {
        return plateRegex;
    }

    public String plateExample() {
        return plateExample;
    }

    public String plateFormat() {
        return plateFormat;
    }

    // Remolque y semirremolque llevan siempre placa verde, sin importar el servicio
    public boolean isTrailer() {
        return this == TRAILER || this == SEMITRAILER;
    }
}