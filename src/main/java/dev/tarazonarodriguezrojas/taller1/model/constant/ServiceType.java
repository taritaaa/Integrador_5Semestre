package dev.tarazonarodriguezrojas.taller1.model.constant;

/**
 * Tipo de servicio de un vehículo: particular, público, oficial o diplomático.
 * Se elige al registrar el vehículo (no en el modelo, porque un mismo modelo puede tener
 * vehículos particulares y públicos).
 * El color de la placa sale del servicio y del tipo de vehículo: ver PlateRules.plateColor.
 */
public enum ServiceType {

    PRIVATE("Particular (privado)"),
    PUBLIC("Público"),
    OFFICIAL("Oficial"),
    DIPLOMATIC("Diplomático");

    private final String label;

    ServiceType(String label) {
        this.label = label;
    }

    // Nombre en español para mostrar en pantalla
    public String label() {
        return label;
    }
}