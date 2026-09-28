package com.actisys.inventoryservice.controller;

import com.actisys.inventoryservice.dto.AddIngredientDTO;
import com.actisys.inventoryservice.dto.IngredientDTO;
import com.actisys.inventoryservice.model.Metrics;
import com.actisys.inventoryservice.service.IngredientService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(IngredientController.class)
class IngredientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IngredientService ingredientService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private IngredientDTO ingredientDTO;

    @BeforeEach
    void setUp() {
        ingredientDTO = IngredientDTO.builder()
                .id(1L)
                .name("Flour")
                .unit(Metrics.KG)
                .currentStock(BigDecimal.valueOf(10))
                .minStock(BigDecimal.valueOf(2))
                .build();
    }

    @Test
    @DisplayName("GET /api/v1/ingredients/getAll возвращает список ингредиентов")
    void getAll_returnsIngredientList() throws Exception {
        given(ingredientService.getAll()).willReturn(List.of(ingredientDTO));

        mockMvc.perform(get("/api/v1/ingredients/getAll"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(1)))
                .andExpect(jsonPath("$[0].name", is("Flour")));
    }

    @Test
    @DisplayName("POST /api/v1/ingredients/addIngredient создаёт ингредиент")
    void addIngredient_createsIngredient() throws Exception {
        AddIngredientDTO addDto = AddIngredientDTO.builder()
                .name("Flour")
                .unit(Metrics.KG)
                .currentStock(BigDecimal.valueOf(10))
                .minStock(BigDecimal.valueOf(2))
                .build();

        given(ingredientService.addNewIngredient(any(AddIngredientDTO.class))).willReturn(ingredientDTO);

        mockMvc.perform(post("/api/v1/ingredients/addIngredient")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Flour")));
    }

    @Test
    @DisplayName("PUT /api/v1/ingredients/pickUpIngredients списывает ингредиенты")
    void pickUpIngredients_returnsOk() throws Exception {
        HashMap<Long, Integer> request = new HashMap<>();
        request.put(1L, 2);

        willDoNothing().given(ingredientService).pickUpIngredients(any());

        mockMvc.perform(put("/api/v1/ingredients/pickUpIngredients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"1\": 10}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /api/v1/ingredients/deleteIngredient/{id} удаляет ингредиент")
    void deleteIngredient_returnsOk() throws Exception {
        willDoNothing().given(ingredientService).deleteIngredient(anyLong());

        mockMvc.perform(delete("/api/v1/ingredients/deleteIngredient/{id}", 1L))
                .andExpect(status().isOk());
    }
}
