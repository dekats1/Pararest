package com.actisys.inventoryservice.service.impl;

import com.actisys.inventoryservice.dto.AddSupplierDTO;
import com.actisys.inventoryservice.dto.SupplierDTO;
import com.actisys.inventoryservice.exception.SupplierNotFoundException;
import com.actisys.inventoryservice.mapper.SupplierMapper;
import com.actisys.inventoryservice.model.Supplier;
import com.actisys.inventoryservice.repository.SupplierRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SupplierServiceImplTest {

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private SupplierMapper supplierMapper;

    @InjectMocks
    private SupplierServiceImpl supplierService;

    private Supplier supplier;
    private SupplierDTO supplierDTO;

    @BeforeEach
    void setUp() {
        supplier = Supplier.builder().id(1L).name("ACME").phone("12345").address("Main St").build();
        supplierDTO = SupplierDTO.builder().id(1L).name("ACME").phone("12345").address("Main St").build();
    }

    @Test
    @DisplayName("getAll() возвращает список поставщиков")
    void getAll_returnsSuppliers() {
        given(supplierRepository.findAll()).willReturn(List.of(supplier));
        given(supplierMapper.toDTO(supplier)).willReturn(supplierDTO);

        List<SupplierDTO> result = supplierService.getAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("ACME");
    }

    @Test
    @DisplayName("addNewSupplier() сохраняет нового поставщика")
    void addNewSupplier_savesSupplier() {
        AddSupplierDTO addDto = AddSupplierDTO.builder().name("ACME").phone("12345").address("Main St").build();

        given(supplierRepository.save(any(Supplier.class))).willReturn(supplier);
        given(supplierMapper.toDTO(supplier)).willReturn(supplierDTO);

        SupplierDTO result = supplierService.addNewSupplier(addDto);

        assertThat(result.getName()).isEqualTo("ACME");
        verify(supplierRepository, times(1)).save(any(Supplier.class));
    }

    @Test
    @DisplayName("editSupplier() обновляет существующего поставщика")
    void editSupplier_existingId_updatesSupplier() {
        AddSupplierDTO updateDto = AddSupplierDTO.builder().name("New Name").phone("999").address("New Address").build();

        given(supplierRepository.findById(1L)).willReturn(Optional.of(supplier));
        given(supplierRepository.save(any(Supplier.class))).willReturn(supplier);
        given(supplierMapper.toDTO(supplier)).willReturn(supplierDTO);

        SupplierDTO result = supplierService.editSupplier(1L, updateDto);

        assertThat(result).isNotNull();
        assertThat(supplier.getName()).isEqualTo("New Name");
        assertThat(supplier.getPhone()).isEqualTo("999");
        assertThat(supplier.getAddress()).isEqualTo("New Address");
    }

    @Test
    @DisplayName("editSupplier() выбрасывает исключение, если поставщик не найден")
    void editSupplier_notFound_throwsException() {
        given(supplierRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> supplierService.editSupplier(99L, new AddSupplierDTO()))
                .isInstanceOf(SupplierNotFoundException.class);
    }

    @Test
    @DisplayName("deleteSupplier() удаляет существующего поставщика")
    void deleteSupplier_existingId_deletesSupplier() {
        given(supplierRepository.findById(1L)).willReturn(Optional.of(supplier));

        supplierService.deleteSupplier(1L);

        verify(supplierRepository, times(1)).delete(supplier);
    }

    @Test
    @DisplayName("deleteSupplier() выбрасывает исключение, если поставщик не найден")
    void deleteSupplier_notFound_throwsException() {
        given(supplierRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> supplierService.deleteSupplier(99L))
                .isInstanceOf(SupplierNotFoundException.class);

        verify(supplierRepository, never()).delete(any());
    }
}
