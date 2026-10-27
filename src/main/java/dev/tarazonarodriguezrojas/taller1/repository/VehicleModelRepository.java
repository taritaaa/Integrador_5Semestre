package dev.tarazonarodriguezrojas.taller1.repository;

import dev.tarazonarodriguezrojas.taller1.model.entity.VehicleModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VehicleModelRepository extends JpaRepository<VehicleModel, Integer> {

    // Para registrar: ¿ya existe un modelo con ese nombre y esa marca?
    boolean existsByNameIgnoreCaseAndBrandIgnoreCase(String name, String brand);

    // Para actualizar: lo mismo, pero ignorando el propio modelo que se edita
    boolean existsByNameIgnoreCaseAndBrandIgnoreCaseAndIdNot(String name, String brand, Integer id);
}