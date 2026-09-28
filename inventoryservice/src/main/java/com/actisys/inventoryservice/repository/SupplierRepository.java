package com.actisys.inventoryservice.repository;

import com.actisys.inventoryservice.model.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {
}
