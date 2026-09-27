package com.actisys.menuservice.service.impl;

import com.actisys.menuservice.dto.AddMenuItemDTO;
import com.actisys.menuservice.dto.MenuItemDTO;
import com.actisys.menuservice.exception.MenuItemAlreadyExistsException;
import com.actisys.menuservice.exception.MenuItemNotFoundException;
import com.actisys.menuservice.mapper.MenuItemMapper;
import com.actisys.menuservice.model.MenuItem;
import com.actisys.menuservice.repository.CategoryRepository;
import com.actisys.menuservice.repository.MenuRepository;
import com.actisys.menuservice.service.MenuService;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenuServiceImpl implements MenuService {

    private final MenuRepository menuRepository;
    private final MenuItemMapper menuItemMapper;
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional(readOnly = true)
    public List<MenuItemDTO> getAllMenu() {
        List<MenuItem> allItems = menuRepository.findAll();
        for(MenuItem item : allItems) {
            System.out.println(item.getAdderName());
            System.out.println(item.getActive());
        }
        List<MenuItemDTO> items = allItems.stream().map(menuItemMapper::toDto).toList();
        for(MenuItemDTO item : items) {
            System.out.println(item.getAdderName());
            System.out.println(item.getActive());
        }
        return allItems.stream().map(menuItemMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public Long addNewMenuItem(String adderName, AddMenuItemDTO newMenuItemDTO) {
        if(menuRepository.existsByName(newMenuItemDTO.getName())) {
            throw new MenuItemAlreadyExistsException(newMenuItemDTO.getName());
        }
        MenuItem menuItem = MenuItem.builder()
                .name(newMenuItemDTO.getName())
                .price(newMenuItemDTO.getPrice())
                .adderName(adderName)
                .imageUrl(newMenuItemDTO.getImageUrl())
                .description(newMenuItemDTO.getDescription())
                .updatedAt(LocalDateTime.now())
                .preparationTime(newMenuItemDTO.getPreparationTime())
                .active(true)
                .category(categoryRepository.findByName(newMenuItemDTO.getCategory().getName()))
                .build();
        return menuRepository.save(menuItem).getId();
    }

    @Override
    public MenuItemDTO editMenuItem(Long id, MenuItemDTO menuItemDTO) {
        return null;
    }

    @Override
    @Transactional
    public MenuItemDTO editAvailability(Long id) {
        MenuItem menuItem = menuRepository.findById(id).orElseThrow(()-> new MenuItemNotFoundException(id));
        menuItem.setActive(!menuItem.getActive());
        return menuItemMapper.toDto(menuRepository.save(menuItem));
    }

    @Override
    public void deleteMenuItem(Long id) {
        MenuItem menuItem = menuRepository.findById(id).orElseThrow(()-> new MenuItemNotFoundException(id));
        menuRepository.delete(menuItem);
    }

    @Override
    @Transactional
    public MenuItemDTO addNewMenuItemNoRecipe(String userName, AddMenuItemDTO newMenuItemDTO) {

        if(menuRepository.existsByName(newMenuItemDTO.getName())) {
            throw new MenuItemAlreadyExistsException(newMenuItemDTO.getName());
        }
        MenuItem menuItem = MenuItem.builder()
                .name(newMenuItemDTO.getName())
                .price(newMenuItemDTO.getPrice())
                .adderName(userName)
                .imageUrl(newMenuItemDTO.getImageUrl())
                .description(newMenuItemDTO.getDescription())
                .updatedAt(LocalDateTime.now())
                .preparationTime(newMenuItemDTO.getPreparationTime())
                .active(true)
                .category(categoryRepository.findByName(newMenuItemDTO.getCategory().getName()))
                .build();
        return menuItemMapper.toDto(menuRepository.save(menuItem));
    }

    @Override
    public List<MenuItemDTO> getMenuItemsByIds(List<Long> menuIds) {
        List<MenuItem> menuItems = menuRepository.findAllByIdIn(menuIds);
        return menuItems.stream().map(menuItemMapper::toDto).collect(Collectors.toList());
    }
}
