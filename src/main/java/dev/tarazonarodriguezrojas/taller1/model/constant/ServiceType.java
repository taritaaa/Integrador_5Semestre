package dev.tarazonarodriguezrojas.taller1.model.constant;

/**
 * Tipo de servicio de un modelo: particular, público, oficial o diplomático.
 * Para público y particular se sugiere el color de placa (amarilla y blanca).
 * Para oficial y diplomático no se sugiere ninguno: lo elige el usuario.
 */
public enum ServiceType {

    PRIVATE("Particular (privado)", PlateColor.WHITE),
    PUBLIC("Público", PlateColor.YELLOW),
    OFFICIAL("Oficial", null),
    DIPLOMATIC("Diplomático", null);

    private final String label;
    private final PlateColor suggestedPlateColor;

    ServiceType(String label, PlateColor suggestedPlateColor) {
        this.label = label;
        this.suggestedPlateColor = suggestedPlateColor;
    }

    public String label() {
        return label;
    }

    // Puede ser nulo (oficial y diplomático)
    public PlateColor suggestedPlateColor() {
        return suggestedPlateColor;
    }
}