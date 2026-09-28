package com.actisys.inventoryservice.repository;

import com.actisys.inventoryservice.model.Supplier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class SupplierRepositoryIT {

    @Autowired
    private SupplierRepository supplierRepository;

    @Test
    @DisplayName("Сохранение и поиск поставщика по id")
    void saveAndFindById() {
        Supplier supplier = Supplier.builder().name("ACME").phone("12345").address("Main St").build();

        Supplier saved = supplierRepository.save(supplier);
        Optional<Supplier> found = supplierRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("ACME");
    }

    @Test
    @DisplayName("delete() удаляет поставщика")
    void delete_removesSupplier() {
        Supplier saved = supplierRepository.save(Supplier.builder().name("ACME").build());

        supplierRepository.delete(saved);

        assertThat(supplierRepository.findById(saved.getId())).isEmpty();
    }
}
