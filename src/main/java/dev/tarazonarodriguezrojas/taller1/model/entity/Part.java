package dev.tarazonarodriguezrojas.taller1.model.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
/**
 * Repuesto del inventario del taller.
 *
 * Relaciones:
 * - Cada repuesto es compatible con un único modelo de vehículo.
 * - Un repuesto puede usarse en muchas reparaciones (a través de RepairPart).
 */
@Entity
@Table(name = "part")
public class Part {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", length = 255)
    private String description;

    // Precio con 10 dígitos en total y 2 decimales (ej: 12345678.90).
    // BigDecimal se usa para dinero porque no pierde precisión como double.
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    // Código de barras EAN-13 (13 dígitos). Único por repuesto.
    @Column(name = "ean", nullable = false, unique = true, length = 13)
    private String ean;

    @Column(name = "stock", nullable = false)
    private Integer stock;

    // Ruta del archivo con la imagen de referencia del repuesto (opcional)
    @Column(name = "image_url", length = 255)
    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "model_id", nullable = false)
    private VehicleModel model;

    @OneToMany(mappedBy = "part")
    private List<RepairPart> repairParts = new ArrayList<>();

    public Part() {
    }

    public Part(String name, String description, BigDecimal price, String ean,
                Integer stock, String imageUrl, VehicleModel model) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.ean = ean;
        this.stock = stock;
        this.imageUrl = imageUrl;
        this.model = model;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getEan() {
        return ean;
    }

    public void setEan(String ean) {
        this.ean = ean;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public VehicleModel getModel() {
        return model;
    }

    public void setModel(VehicleModel model) {
        this.model = model;
    }

    public List<RepairPart> getRepairParts() {
        return repairParts;
    }

    public void setRepairParts(List<RepairPart> repairParts) {
        this.repairParts = repairParts;
    }
}

