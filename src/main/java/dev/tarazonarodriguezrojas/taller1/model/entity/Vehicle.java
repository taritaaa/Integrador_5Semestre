package dev.tarazonarodriguezrojas.taller1.model.entity;

import dev.tarazonarodriguezrojas.taller1.model.constant.PlateColor;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
/**
 * Vehículo que ingresa al taller. Se identifica por su placa.
 *
 * Relaciones:
 * - Muchos vehículos pertenecen a un modelo.
 * - Un vehículo recibe muchas reparaciones.
 */
@Entity
@Table(name = "vehicle")
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Placa del vehículo. Es única: no puede haber dos vehículos con la misma placa.
    // Formatos válidos: AAA-123, AAA-12A, AAA-12, R-12345, S-12345, CC-1234, CD-1234
    // Todos caben en 7 caracteres. La validación del formato se hará en el servicio.
    @Column(name = "license_plate", nullable = false, unique = true, length = 7)
    private String licensePlate;

    // Color de la placa. Se guarda como texto ("YELLOW", "WHITE", etc.)
    @Enumerated(EnumType.STRING)
    @Column(name = "plate_color", nullable = false, length = 20)
    private PlateColor plateColor;

    // Datos del propietario del vehículo
    @Column(name = "owner_name", nullable = false, length = 100)
    private String ownerName;

    @Column(name = "owner_document", nullable = false, length = 20)
    private String ownerDocument;

    @Column(name = "owner_phone", nullable = false, length = 20)
    private String ownerPhone;

    // Modelo del vehículo. Crea la columna model_id (llave foránea).
    // LAZY: el modelo se carga solo cuando se pide.
    // optional = false: todo vehículo debe tener un modelo.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "model_id", nullable = false)
    private VehicleModel model;

    // Historial de reparaciones de este vehículo.
    // mappedBy = "vehicle": la llave foránea está en el atributo "vehicle" de Repair
    @OneToMany(mappedBy = "vehicle")
    private List<Repair> repairs = new ArrayList<>();

    public Vehicle() {
    }

    public Vehicle(String licensePlate, PlateColor plateColor, String ownerName,
                   String ownerDocument, String ownerPhone, VehicleModel model) {
        this.licensePlate = licensePlate;
        this.plateColor = plateColor;
        this.ownerName = ownerName;
        this.ownerDocument = ownerDocument;
        this.ownerPhone = ownerPhone;
        this.model = model;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public PlateColor getPlateColor() {
        return plateColor;
    }

    public void setPlateColor(PlateColor plateColor) {
        this.plateColor = plateColor;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getOwnerDocument() {
        return ownerDocument;
    }

    public void setOwnerDocument(String ownerDocument) {
        this.ownerDocument = ownerDocument;
    }

    public String getOwnerPhone() {
        return ownerPhone;
    }

    public void setOwnerPhone(String ownerPhone) {
        this.ownerPhone = ownerPhone;
    }

    public VehicleModel getModel() {
        return model;
    }

    public void setModel(VehicleModel model) {
        this.model = model;
    }

    public List<Repair> getRepairs() {
        return repairs;
    }

    public void setRepairs(List<Repair> repairs) {
        this.repairs = repairs;
    }
}