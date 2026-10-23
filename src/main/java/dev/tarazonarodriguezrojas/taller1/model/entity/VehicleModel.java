package dev.tarazonarodriguezrojas.taller1.model.entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
/**
 * Modelo de vehículo (por ejemplo: "Spark GT", marca "Chevrolet").
 * Se llama VehicleModel y no Model para no confundirse con
 * org.springframework.ui.Model, que se usa en los controladores.
 *
 * Relaciones:
 * - Un modelo clasifica muchos vehículos.
 * - Un modelo tiene muchos repuestos compatibles.
 */
@Entity
@Table(name = "model")
public class VehicleModel {
    // Llave primaria, generada automáticamente por la base de datos
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Nombre del modelo, obligatorio, máximo 100 caracteres
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    // Marca del modelo, obligatoria, máximo 100 caracteres
    @Column(name = "brand", nullable = false, length = 100)
    private String brand;

    // Lista de vehículos de este modelo.
    // mappedBy = "model": la llave foránea está en el atributo "model" de Vehicle
    @OneToMany(mappedBy = "model")
    private List<Vehicle> vehicles = new ArrayList<>();

    // Lista de repuestos compatibles con este modelo.
    // mappedBy = "model": la llave foránea está en el atributo "model" de Part
    @OneToMany(mappedBy = "model")
    private List<Part> parts = new ArrayList<>();

    // Constructor vacío: JPA lo necesita para crear objetos desde la base de datos
    public VehicleModel() {
    }
    // Constructor para crear un modelo nuevo (el id lo genera la base de datos)
    public VehicleModel(String name, String brand) {
        this.name = name;
        this.brand = brand;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public List<Vehicle> getVehicles() {
        return vehicles;
    }

    public void setVehicles(List<Vehicle> vehicles) {
        this.vehicles = vehicles;
    }

    public List<Part> getParts() {
        return parts;
    }

    public void setParts(List<Part> parts) {
        this.parts = parts;
    }
}
