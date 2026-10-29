package dev.tarazonarodriguezrojas.taller1.repository;

import dev.tarazonarodriguezrojas.taller1.model.constant.RepairStatus;
import dev.tarazonarodriguezrojas.taller1.model.entity.Repair;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RepairRepository extends JpaRepository<Repair, Integer> {

    // RF-22: historial filtrado por placa (sin guiones, coincidencia parcial). Las más recientes primero.
    @Query("select r from Repair r join r.vehicle v "
            + "where upper(replace(v.licensePlate, '-', '')) like concat('%', :plate, '%') "
            + "order by r.date desc, r.id desc")
    List<Repair> searchByPlate(@Param("plate") String plate);

    // RF-22: historial filtrado por placa y por estado
    @Query("select r from Repair r join r.vehicle v "
            + "where upper(replace(v.licensePlate, '-', '')) like concat('%', :plate, '%') "
            + "and r.status = :status "
            + "order by r.date desc, r.id desc")
    List<Repair> searchByPlateAndStatus(@Param("plate") String plate, @Param("status") RepairStatus status);

    // RF-28: reparaciones que ya tienen un punto marcado en el mapa
    List<Repair> findByWorkshopLatitudeIsNotNullAndWorkshopLongitudeIsNotNull();
}