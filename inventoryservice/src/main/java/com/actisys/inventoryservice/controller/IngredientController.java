package com.actisys.inventoryservice.controller;

import com.actisys.inventoryservice.dto.AddIngredientDTO;
import com.actisys.inventoryservice.dto.IngredientDTO;
import com.actisys.inventoryservice.service.IngredientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/ingredients")
public class IngredientController {

    private final IngredientService ingredientService;

    @GetMapping("/getAll")
    public ResponseEntity<List<IngredientDTO>> getAll() {
        return ResponseEntity.ok().body(ingredientService.getAll());
    }

    @PostMapping("/addIngredient")
    public ResponseEntity<IngredientDTO> addIngredient(@RequestBody AddIngredientDTO ingredient) {
        return ResponseEntity.ok().body(ingredientService.addNewIngredient(ingredient));
    }

    @PutMapping("/pickUpIngredients")
    public ResponseEntity<Void> pickUpIngredients(@RequestBody HashMap<Long, Integer> ingredients) {
        ingredientService.pickUpIngredients(ingredients);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/deleteIngredient/{id}")
    public ResponseEntity<Void> deleteIngredient(@PathVariable Long id) {
        ingredientService.deleteIngredient(id);
        return ResponseEntity.ok().build();
    }

}
