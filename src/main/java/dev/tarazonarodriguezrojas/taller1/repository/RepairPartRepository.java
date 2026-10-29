package dev.tarazonarodriguezrojas.taller1.repository;

import dev.tarazonarodriguezrojas.taller1.model.entity.RepairPart;
import dev.tarazonarodriguezrojas.taller1.model.entity.RepairPartId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RepairPartRepository extends JpaRepository<RepairPart, RepairPartId> {
}