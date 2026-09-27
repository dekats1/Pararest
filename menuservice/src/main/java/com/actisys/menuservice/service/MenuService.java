package com.actisys.menuservice.service;

import com.actisys.menuservice.dto.AddMenuItemDTO;
import com.actisys.menuservice.dto.MenuItemDTO;
import org.jspecify.annotations.Nullable;

import java.util.List;

public interface MenuService {
    List<MenuItemDTO> getAllMenu();

    Long addNewMenuItem(String userName, AddMenuItemDTO newMenuItemDTO);

    MenuItemDTO editMenuItem(Long id, MenuItemDTO menuItemDTO);

    MenuItemDTO editAvailability(Long id);

    void deleteMenuItem(Long id);

    MenuItemDTO addNewMenuItemNoRecipe(String userName, AddMenuItemDTO newMenuItemDTO);

    List<MenuItemDTO> getMenuItemsByIds(List<Long> menuIds);
}
