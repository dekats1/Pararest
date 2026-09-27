package com.actisys.inventoryservice.mapper;

import com.actisys.inventoryservice.dto.SupplierDTO;
import com.actisys.inventoryservice.model.Supplier;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

@Mapper(componentModel = "spring")
@Component
public interface SupplierMapper {
    SupplierDTO toDTO(Supplier supplier);
    Supplier toEntity(SupplierDTO supplierDTO);
}
