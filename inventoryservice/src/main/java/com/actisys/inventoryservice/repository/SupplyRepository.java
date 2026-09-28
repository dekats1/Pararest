package com.actisys.inventoryservice.repository;

import com.actisys.inventoryservice.model.Supply;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupplyRepository extends JpaRepository<Supply, Long> {
}
