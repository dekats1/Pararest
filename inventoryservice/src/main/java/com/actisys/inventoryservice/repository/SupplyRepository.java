package com.actisys.inventoryservice.repository;

import com.actisys.inventoryservice.model.Supply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SupplyRepository extends JpaRepository<Supply, Long> {
}
