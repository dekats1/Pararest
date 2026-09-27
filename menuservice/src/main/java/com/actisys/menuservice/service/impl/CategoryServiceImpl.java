package com.actisys.menuservice.service.impl;

import com.actisys.menuservice.dto.CategoryDTO;
import com.actisys.menuservice.exception.CategoryAlreadyExistsException;
import com.actisys.menuservice.exception.CategoryNotFoundException;
import com.actisys.menuservice.mapper.MenuCategoryMapper;
import com.actisys.menuservice.model.MenuCategory;
import com.actisys.menuservice.repository.CategoryRepository;
import com.actisys.menuservice.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final MenuCategoryMapper menuCategoryMapper;

    @Override
    public List<CategoryDTO> getAll() {
        List<MenuCategory> categories = categoryRepository.findAll();

        return categories.stream().map(menuCategoryMapper::toCategoryDto).collect(Collectors.toList());
    }

    @Override
    public CategoryDTO addNew(CategoryDTO categoryDTO) {
        if(categoryRepository.existsByName(categoryDTO.getName())){
            throw new CategoryAlreadyExistsException(categoryDTO.getName());
        }
        MenuCategory category = new MenuCategory();
        category.setName(categoryDTO.getName());
        return menuCategoryMapper.toCategoryDto(categoryRepository.save(category));
    }

    @Override
    public void delete(Long id) {
        MenuCategory category = categoryRepository.findById(id).orElseThrow(()->new CategoryNotFoundException(id));
        categoryRepository.delete(category);
    }
}
