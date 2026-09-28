package com.actisys.inventoryservice.repository;

import com.actisys.inventoryservice.model.Supplier;
import com.actisys.inventoryservice.model.Supply;
import com.actisys.inventoryservice.model.SupplyStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class SupplyRepositoryIT {

    @Autowired
    private SupplyRepository supplyRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    @Test
    @DisplayName("Сохранение и получение всех поставок")
    void saveAndFindAll() {
        Supplier supplier = supplierRepository.save(Supplier.builder().name("ACME").build());

        Supply supply = Supply.builder()
                .supplier(supplier)
                .dateOfRequest(LocalDateTime.now())
                .status(SupplyStatus.IN_SORT_CENTER)
                .totalAmount(BigDecimal.ZERO)
                .build();

        supplyRepository.save(supply);

        List<Supply> all = supplyRepository.findAll();

        assertThat(all).hasSize(1);
        assertThat(all.get(0).getSupplier().getName()).isEqualTo("ACME");
    }
}
