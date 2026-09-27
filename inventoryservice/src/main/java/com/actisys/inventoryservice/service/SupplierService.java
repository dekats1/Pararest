package com.actisys.inventoryservice.service;

import com.actisys.inventoryservice.dto.AddSupplierDTO;
import com.actisys.inventoryservice.dto.SupplierDTO;
import org.jspecify.annotations.Nullable;

import java.util.List;

public interface SupplierService {
    List<SupplierDTO> getAll();

    SupplierDTO addNewSupplier(AddSupplierDTO supplierDTO);

    SupplierDTO editSupplier(Long id, AddSupplierDTO supplierDTO);

    void deleteSupplier(Long id);
}
