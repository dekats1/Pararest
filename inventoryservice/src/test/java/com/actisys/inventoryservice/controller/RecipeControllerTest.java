package com.actisys.inventoryservice.controller;

import com.actisys.dto.AddRecipeDTO;
import com.actisys.inventoryservice.dto.IngredientDTO;
import com.actisys.inventoryservice.dto.RecipeDTO;
import com.actisys.inventoryservice.model.RecipeId;
import com.actisys.inventoryservice.service.RecipeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(RecipeController.class)
class RecipeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RecipeService recipeService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("POST /api/v1/recipes/addNew создаёт рецепт")
    void addNewRecipe_returnsTrue() throws Exception {
        HashMap<String, Integer> ingredientsMap = new LinkedHashMap<>();
        ingredientsMap.put("1", 2);
        AddRecipeDTO addRecipeDTO = AddRecipeDTO.builder()
                .menuItemId(10L)
                .ingredientsMap(ingredientsMap)
                .build();

        given(recipeService.addNewRecipe(any(AddRecipeDTO.class))).willReturn(true);

        mockMvc.perform(post("/api/v1/recipes/addNew")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addRecipeDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is(true)));
    }

    @Test
    @DisplayName("GET /api/v1/recipes/getRecipeByMenuId/{id} возвращает рецепт")
    void getRecipeByMenuId_returnsRecipeList() throws Exception {
        RecipeDTO recipeDTO = RecipeDTO.builder()
                .id(new RecipeId(10L, 1L))
                .ingredient(IngredientDTO.builder().id(1L).name("Cheese").build())
                .quantity(2)
                .build();

        given(recipeService.getRecipeByMenuId(10L)).willReturn(List.of(recipeDTO));

        mockMvc.perform(get("/api/v1/recipes/getRecipeByMenuId/{id}", 10L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(1)))
                .andExpect(jsonPath("$[0].quantity", is(2)));
    }
}
