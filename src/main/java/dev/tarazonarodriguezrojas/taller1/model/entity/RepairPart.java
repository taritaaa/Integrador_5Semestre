package dev.tarazonarodriguezrojas.taller1.model.entity;

import jakarta.persistence.*;
/**
 * Tabla intermedia entre Repair y Part.
 * Guarda qué repuestos se usaron en una reparación y en qué cantidad
 * (RF-21 y RF-25).
 */
@Entity
@Table(name = "repair_part")
public class RepairPart {

    // Llave primaria compuesta (repair_id + part_id)
    @EmbeddedId
    private RepairPartId id = new RepairPartId();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("repairId")
    @JoinColumn(name = "repair_id", nullable = false)
    private Repair repair;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("partId")
    @JoinColumn(name = "part_id", nullable = false)
    private Part part;

    // Cantidad de unidades del repuesto usadas en la reparación.
    // El servicio de reparaciones descontará esta cantidad del stock.
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    public RepairPart() {
    }

    // La reparación y el repuesto deben estar guardados (tener id)
    // antes de crear este registro.
    public RepairPart(Repair repair, Part part, Integer quantity) {
        this.repair = repair;
        this.part = part;
        this.quantity = quantity;
        this.id = new RepairPartId(repair.getId(), part.getId());
    }

    public RepairPartId getId() {
        return id;
    }

    public void setId(RepairPartId id) {
        this.id = id;
    }

    public Repair getRepair() {
        return repair;
    }

    public void setRepair(Repair repair) {
        this.repair = repair;
    }

    public Part getPart() {
        return part;
    }

    public void setPart(Part part) {
        this.part = part;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
