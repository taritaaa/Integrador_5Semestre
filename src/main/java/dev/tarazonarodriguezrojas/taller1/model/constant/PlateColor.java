package dev.tarazonarodriguezrojas.taller1.model.constant;


/**
 * Colores posibles de la placa de un vehículo.
 * Se usa en la entidad Vehicle. Casi siempre se calcula con PlateRules.plateColor;
 * solo en los vehículos oficiales lo elige el usuario.
 */
public enum PlateColor {
    YELLOW("Amarilla"),
    WHITE("Blanca"),
    RED("Roja"),
    BLUE("Azul"),
    GREEN("Verde");

    private final String label;

    PlateColor(String label) {
        this.label = label;
    }

    // Nombre en español para mostrar en pantalla
    public String label() {
        return label;
    }
}
