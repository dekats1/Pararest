package com.actisys.menuservice.mapper;

import com.actisys.menuservice.dto.MenuItemDTO;
import com.actisys.menuservice.model.MenuItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.stereotype.Component;

@Component
@Mapper(componentModel = "spring", uses = MenuCategoryMapper.class)
public interface MenuItemMapper {
    @Mapping(target = "active", source = "active")
    @Mapping(target = "adderName", source = "adderName")
    MenuItemDTO toDto(MenuItem menuItem);
}
