package dev.tarazonarodriguezrojas.taller1.model.constant;
/**
 * Estados posibles de una reparación.
 * IN_PROGRESS: en proceso.
 * COMPLETED: terminada.
 * CANCELLED: cancelada (al cancelar, el stock del repuesto se devuelve).
 */
public enum RepairStatus {
    IN_PROGRESS,
    COMPLETED,
    CANCELLED
}
