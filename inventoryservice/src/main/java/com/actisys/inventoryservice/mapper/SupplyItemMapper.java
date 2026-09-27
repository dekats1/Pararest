package com.actisys.inventoryservice.mapper;

import com.actisys.inventoryservice.dto.SupplyItemDTO;
import com.actisys.inventoryservice.model.SupplyItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.stereotype.Component;

@Component
@Mapper(componentModel = "spring")
public interface SupplyItemMapper {
    @Mapping(target = "supply", ignore = true)
    SupplyItemDTO toDTO(SupplyItem supplyItem);
}
