package com.actisys.inventoryservice.mapper;

import com.actisys.inventoryservice.dto.SupplyDTO;
import com.actisys.inventoryservice.model.Supply;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.stereotype.Component;

@Component
@Mapper(componentModel = "spring", uses = {SupplierMapper.class, SupplyItemMapper.class})
public interface SupplyMapper {
    @Mapping(target = "totalPrice", source = "totalAmount")
    SupplyDTO toDTO(Supply supply);
    @Mapping(target = "totalAmount", source = "totalPrice")
    Supply toEntity(SupplyDTO supplyDTO);
}
