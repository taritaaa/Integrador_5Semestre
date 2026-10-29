package dev.tarazonarodriguezrojas.taller1.repository;

import dev.tarazonarodriguezrojas.taller1.model.entity.Part;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PartRepository extends JpaRepository<Part, Integer> {

    // RF-16: ¿ya existe un repuesto con ese código de barras?
    boolean existsByEan(String ean);

    // RF-26: repuestos compatibles con un modelo
    List<Part> findByModelId(Integer modelId);
}