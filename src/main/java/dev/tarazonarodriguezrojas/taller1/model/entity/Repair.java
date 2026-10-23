package dev.tarazonarodriguezrojas.taller1.model.entity;

import dev.tarazonarodriguezrojas.taller1.model.constant.RepairStatus;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
/**
 * Reparación realizada a un vehículo por un mecánico.
 *
 * Relaciones:
 * - Muchas reparaciones pertenecen a un vehículo.
 * - Muchas reparaciones son atendidas por un mecánico (User).
 * - Una reparación puede usar varios repuestos (a través de RepairPart).
 */
@Entity
@Table(name = "repair")
public class Repair {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "type", nullable = false, length = 100)
    private String type;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private RepairStatus status;

    // Costo de la mano de obra (2 decimales)
    @Column(name = "labor_cost", nullable = false, precision = 10, scale = 2)
    private BigDecimal laborCost;

    // Datos del taller donde se compró el repuesto: nombre y punto en el mapa
    // (latitud y longitud). Son opcionales.
    @Column(name = "workshop_name", length = 100)
    private String workshopName;

    @Column(name = "workshop_latitude")
    private Double workshopLatitude;

    @Column(name = "workshop_longitude")
    private Double workshopLongitude;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    // Mecánico que atiende la reparación. Crea la columna mechanic_id,
    // que apunta a la tabla de usuarios.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mechanic_id", nullable = false)
    private User mechanic;

    @OneToMany(mappedBy = "repair")
    private List<RepairPart> repairParts = new ArrayList<>();

    public Repair() {
    }

    public Repair(LocalDate date, String type, RepairStatus status, BigDecimal laborCost,
                  String workshopName, Double workshopLatitude, Double workshopLongitude,
                  Vehicle vehicle, User mechanic) {
        this.date = date;
        this.type = type;
        this.status = status;
        this.laborCost = laborCost;
        this.workshopName = workshopName;
        this.workshopLatitude = workshopLatitude;
        this.workshopLongitude = workshopLongitude;
        this.vehicle = vehicle;
        this.mechanic = mechanic;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public RepairStatus getStatus() {
        return status;
    }

    public void setStatus(RepairStatus status) {
        this.status = status;
    }

    public BigDecimal getLaborCost() {
        return laborCost;
    }

    public void setLaborCost(BigDecimal laborCost) {
        this.laborCost = laborCost;
    }

    public String getWorkshopName() {
        return workshopName;
    }

    public void setWorkshopName(String workshopName) {
        this.workshopName = workshopName;
    }

    public Double getWorkshopLatitude() {
        return workshopLatitude;
    }

    public void setWorkshopLatitude(Double workshopLatitude) {
        this.workshopLatitude = workshopLatitude;
    }

    public Double getWorkshopLongitude() {
        return workshopLongitude;
    }

    public void setWorkshopLongitude(Double workshopLongitude) {
        this.workshopLongitude = workshopLongitude;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    public User getMechanic() {
        return mechanic;
    }

    public void setMechanic(User mechanic) {
        this.mechanic = mechanic;
    }

    public List<RepairPart> getRepairParts() {
        return repairParts;
    }

    public void setRepairParts(List<RepairPart> repairParts) {
        this.repairParts = repairParts;
    }
}