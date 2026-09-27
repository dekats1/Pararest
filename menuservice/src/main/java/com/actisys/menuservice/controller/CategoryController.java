package com.actisys.menuservice.controller;

import com.actisys.menuservice.dto.CategoryDTO;
import com.actisys.menuservice.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    @GetMapping("/getAll")
    public ResponseEntity<List<CategoryDTO>> getAll() {
        return ResponseEntity.ok(categoryService.getAll());
    }

    @PostMapping("/addNew")
    public ResponseEntity<CategoryDTO> addNew(@RequestBody CategoryDTO categoryDTO) {
        return ResponseEntity.ok(categoryService.addNew(categoryDTO));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return ResponseEntity.ok().build();
    }
}
