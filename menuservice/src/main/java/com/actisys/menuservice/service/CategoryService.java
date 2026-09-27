package com.actisys.menuservice.service;

import com.actisys.menuservice.dto.CategoryDTO;

import java.util.List;

public interface CategoryService {
    List<CategoryDTO> getAll();

    CategoryDTO addNew(CategoryDTO categoryDTO);

    void delete(Long id);
}
