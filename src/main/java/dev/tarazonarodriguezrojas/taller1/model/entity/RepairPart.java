package dev.tarazonarodriguezrojas.taller1.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

import java.math.BigDecimal;

/**
 * Tabla intermedia entre Repair y Part.
 * Guarda qué repuesto se usó en una reparación, en qué cantidad y a qué precio
 * (el precio al momento de usarlo, para que el costo histórico no cambie).
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

    // Cantidad de unidades del repuesto usadas en la reparación
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    // Precio de una unidad al momento de usar el repuesto
    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    public RepairPart() {
    }

    // La reparación y el repuesto deben estar guardados (tener id)
    // antes de crear este registro.
    public RepairPart(Repair repair, Part part, Integer quantity, BigDecimal unitPrice) {
        this.repair = repair;
        this.part = part;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
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

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }
}