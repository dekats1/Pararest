package com.actisys.inventoryservice.controller;

import com.actisys.dto.AddRecipeDTO;
import com.actisys.inventoryservice.dto.RecipeDTO;
import com.actisys.inventoryservice.service.RecipeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/recipes")
@RequiredArgsConstructor
public class RecipeController {

    private final RecipeService recipeService;

    @PostMapping("/addNew")
    public ResponseEntity<Boolean> addNewRecipe(@RequestBody AddRecipeDTO addRecipeDTO) {
        return ResponseEntity.ok().body(recipeService.addNewRecipe(addRecipeDTO));
    }

    @GetMapping("/getRecipeByMenuId/{id}")
    public ResponseEntity<List<RecipeDTO>> getRecipeByMenuId(@PathVariable Long id) {
        return ResponseEntity.ok().body(recipeService.getRecipeByMenuId(id));
    }
}
