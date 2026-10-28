package dev.tarazonarodriguezrojas.taller1.repository;

import dev.tarazonarodriguezrojas.taller1.model.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Integer> {

    // RF-12: ¿ya existe un vehículo con esa placa?
    boolean existsByLicensePlate(String licensePlate);

    // RF-27: buscar un vehículo por su placa exacta
    Optional<Vehicle> findByLicensePlate(String licensePlate);

    // RF-13: filtrar por placa y/o propietario.
    // La placa se compara sin guiones, así "abc12" encuentra "ABC-12A" y "R123" encuentra "R-12345".
    // Si un filtro viene vacío, no restringe nada.
    @Query("select v from Vehicle v "
            + "where upper(replace(v.licensePlate, '-', '')) like concat('%', :plate, '%') "
            + "and lower(v.ownerName) like lower(concat('%', :owner, '%'))")
    List<Vehicle> searchVehicles(@Param("plate") String plate, @Param("owner") String owner);
}
