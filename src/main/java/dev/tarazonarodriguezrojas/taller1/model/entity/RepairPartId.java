package dev.tarazonarodriguezrojas.taller1.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

/**
 * Llave primaria compuesta de RepairPart: la unión de repair_id y part_id.
 *
 * Es necesaria porque la tabla repair_part no tiene un id propio;
 * cada fila se identifica por la pareja (reparación, repuesto).
 *
 * Reglas de JPA para una llave compuesta:
 * - Debe ser @Embeddable.
 * - Debe implementar Serializable.
 * - Debe tener equals y hashCode, para poder comparar dos llaves.
 */
@Embeddable
public class RepairPartId implements Serializable {

    @Column(name = "repair_id")
    private Integer repairId;

    @Column(name = "part_id")
    private Integer partId;

    public RepairPartId() {
    }

    public RepairPartId(Integer repairId, Integer partId) {
        this.repairId = repairId;
        this.partId = partId;
    }

    public Integer getRepairId() {
        return repairId;
    }

    public void setRepairId(Integer repairId) {
        this.repairId = repairId;
    }

    public Integer getPartId() {
        return partId;
    }

    public void setPartId(Integer partId) {
        this.partId = partId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RepairPartId that = (RepairPartId) o;
        return Objects.equals(repairId, that.repairId) && Objects.equals(partId, that.partId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(repairId, partId);
    }
}
