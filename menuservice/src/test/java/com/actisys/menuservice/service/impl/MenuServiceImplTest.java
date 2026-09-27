package com.actisys.menuservice.service.impl;

import com.actisys.menuservice.dto.MenuCategoryDTO;
import com.actisys.menuservice.dto.AddMenuItemDTO;
import com.actisys.menuservice.dto.MenuItemDTO;
import com.actisys.menuservice.mapper.MenuItemMapper;
import com.actisys.menuservice.model.MenuCategory;
import com.actisys.menuservice.model.MenuItem;
import com.actisys.menuservice.repository.CategoryRepository;
import com.actisys.menuservice.repository.MenuRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MenuServiceImplTest {

    @Mock
    private MenuRepository menuRepository;
    @Mock
    private MenuItemMapper menuItemMapper;
    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private MenuServiceImpl menuService;

    @Test
    void addNewMenuItemNoRecipe_savesAndMapsCreatedItem() {
        AddMenuItemDTO dto = AddMenuItemDTO.builder()
                .name("Pasta")
                .description("Nice")
                .price(new BigDecimal("25.50"))
                .category(MenuCategoryDTO.builder().name("Main").build())
                .imageUrl("img")
                .preparationTime(20)
                .build();

        MenuCategory category = MenuCategory.builder().id(1L).name("Main").build();
        MenuItem saved = MenuItem.builder().id(10L).name("Pasta").category(category).active(true).build();
        MenuItemDTO expected = MenuItemDTO.builder().id(10L).name("Pasta").build();

        when(menuRepository.existsByName("Pasta")).thenReturn(false);
        when(categoryRepository.findByName("Main")).thenReturn(category);
        when(menuRepository.save(any(MenuItem.class))).thenReturn(saved);
        when(menuItemMapper.toDto(saved)).thenReturn(expected);

        MenuItemDTO actual = menuService.addNewMenuItemNoRecipe("manager", dto);

        assertEquals(10L, actual.getId());
        assertEquals("Pasta", actual.getName());
    }
}
