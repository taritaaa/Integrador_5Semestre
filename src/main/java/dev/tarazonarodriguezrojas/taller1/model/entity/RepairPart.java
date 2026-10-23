package dev.tarazonarodriguezrojas.taller1.model.entity;

import jakarta.persistence.*;
/**
 * Tabla intermedia entre Repair y Part.
 * Una reparación puede usar muchos repuestos y un repuesto puede
 * usarse en muchas reparaciones (relación muchos a muchos).
 *
 * Según el diagrama, esta tabla solo tiene repair_id y part_id.
 * Falta la columna "quantity" (cantidad usada) que piden RF-21 y RF-25;
 * conviene agregarla al diagrama y a esta clase.
 */
@Entity
@Table(name = "repair_part")
public class RepairPart {
    //CLAVE COMPUESTA: En lugar de tener un atributo simple @Id Integer id;, agrupa los dos IDs necesarios (repairId y partId) dentro del objeto id.
    @EmbeddedId
    private RepairPartId id = new RepairPartId();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    //Vincula un campo de la clave compuesta (RepairPartId) con una relación @ManyToOne
    @MapsId("repairId")
    @JoinColumn(name = "repair_id", nullable = false)
    private Repair repair;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("partId")
    @JoinColumn(name = "part_id", nullable = false)
    private Part part;

    public RepairPart() {
    }

    public RepairPart(Repair repair, Part part) {
        this.repair = repair;
        this.part = part;
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
}
