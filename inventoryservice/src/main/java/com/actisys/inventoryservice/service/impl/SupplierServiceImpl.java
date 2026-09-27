package com.actisys.inventoryservice.service.impl;

import com.actisys.inventoryservice.dto.AddSupplierDTO;
import com.actisys.inventoryservice.dto.SupplierDTO;
import com.actisys.inventoryservice.exception.SupplierNotFoundException;
import com.actisys.inventoryservice.mapper.SupplierMapper;
import com.actisys.inventoryservice.model.Supplier;
import com.actisys.inventoryservice.repository.SupplierRepository;
import com.actisys.inventoryservice.service.SupplierService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SupplierServiceImpl implements SupplierService {

    private final SupplierRepository supplierRepository;
    private final SupplierMapper supplierMapper;

    @Override
    public List<SupplierDTO> getAll() {
        return supplierRepository.findAll().stream()
                .map(supplierMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public SupplierDTO addNewSupplier(AddSupplierDTO supplierDTO) {
        Supplier supplier = Supplier.builder()
                .name(supplierDTO.getName())
                .phone(supplierDTO.getPhone())
                .address(supplierDTO.getAddress())
                .build();
        return supplierMapper.toDTO(supplierRepository.save(supplier));
    }

    @Override
    public SupplierDTO editSupplier(Long id, AddSupplierDTO supplierDTO) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new SupplierNotFoundException(id));
        supplier.setName(supplierDTO.getName());
        supplier.setPhone(supplierDTO.getPhone());
        supplier.setAddress(supplierDTO.getAddress());
        return supplierMapper.toDTO(supplierRepository.save(supplier));
    }

    @Override
    public void deleteSupplier(Long id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new SupplierNotFoundException(id));
        supplierRepository.delete(supplier);
    }
}
