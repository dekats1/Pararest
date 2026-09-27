package com.actisys.menuservice.mapper;

import com.actisys.menuservice.dto.CategoryDTO;
import com.actisys.menuservice.dto.MenuCategoryDTO;
import com.actisys.menuservice.model.MenuCategory;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

@Component
@Mapper(componentModel = "spring")
public interface MenuCategoryMapper {
    MenuCategoryDTO toMenuCategoryDto(MenuCategory menuCategory);
    CategoryDTO toCategoryDto(MenuCategory category);
}
