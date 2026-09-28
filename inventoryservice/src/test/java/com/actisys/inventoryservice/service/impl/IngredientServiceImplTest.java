package com.actisys.inventoryservice.service.impl;

import com.actisys.inventoryservice.dto.AddIngredientDTO;
import com.actisys.inventoryservice.dto.IngredientDTO;
import com.actisys.inventoryservice.exception.IngredientNotFoundException;
import com.actisys.inventoryservice.mapper.IngredientMapper;
import com.actisys.inventoryservice.model.Ingredient;
import com.actisys.inventoryservice.model.Metrics;
import com.actisys.inventoryservice.repository.IngredientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class IngredientServiceImplTest {

    @Mock
    private IngredientRepository ingredientRepository;

    @Mock
    private IngredientMapper ingredientMapper;

    @InjectMocks
    private IngredientServiceImpl ingredientService;

    private Ingredient ingredient;
    private IngredientDTO ingredientDTO;

    @BeforeEach
    void setUp() {
        ingredient = Ingredient.builder()
                .id(1L)
                .name("Flour")
                .unit(Metrics.KG)
                .currentStock(BigDecimal.valueOf(10))
                .minStock(BigDecimal.valueOf(2))
                .updatedAt(LocalDateTime.now())
                .build();

        ingredientDTO = IngredientDTO.builder()
                .id(1L)
                .name("Flour")
                .unit(Metrics.KG)
                .currentStock(BigDecimal.valueOf(10))
                .minStock(BigDecimal.valueOf(2))
                .build();
    }

    @Test
    @DisplayName("getAll() возвращает список всех ингредиентов")
    void getAll_returnsAllIngredients() {
        given(ingredientRepository.findAll()).willReturn(List.of(ingredient));
        given(ingredientMapper.toDto(ingredient)).willReturn(ingredientDTO);

        List<IngredientDTO> result = ingredientService.getAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Flour");
        verify(ingredientRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("addNewIngredient() сохраняет новый ингредиент")
    void addNewIngredient_savesIngredient() {
        AddIngredientDTO addDto = AddIngredientDTO.builder()
                .name("Sugar")
                .unit(Metrics.KG)
                .currentStock(BigDecimal.valueOf(5))
                .minStock(BigDecimal.valueOf(1))
                .build();

        given(ingredientRepository.save(any(Ingredient.class))).willReturn(ingredient);
        given(ingredientMapper.toDto(ingredient)).willReturn(ingredientDTO);

        IngredientDTO result = ingredientService.addNewIngredient(addDto);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Flour");
        verify(ingredientRepository, times(1)).save(any(Ingredient.class));
    }

    @Test
    @DisplayName("deleteIngredient() удаляет существующий ингредиент")
    void deleteIngredient_existingId_deletesIngredient() {
        given(ingredientRepository.findById(1L)).willReturn(Optional.of(ingredient));

        ingredientService.deleteIngredient(1L);

        verify(ingredientRepository, times(1)).delete(ingredient);
    }

    @Test
    @DisplayName("deleteIngredient() выбрасывает исключение, если ингредиент не найден")
    void deleteIngredient_notFound_throwsException() {
        given(ingredientRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> ingredientService.deleteIngredient(99L))
                .isInstanceOf(IngredientNotFoundException.class);

        verify(ingredientRepository, never()).delete(any());
    }

    @Test
    @DisplayName("pickUpIngredients() уменьшает остаток ингредиентов")
    void pickUpIngredients_reducesStock() {
        HashMap<Long, Integer> request = new HashMap<>();
        request.put(1L, 3);

        given(ingredientRepository.findById(1L)).willReturn(Optional.of(ingredient));
        given(ingredientRepository.save(any(Ingredient.class))).willReturn(ingredient);

        ingredientService.pickUpIngredients(request);

        assertThat(ingredient.getCurrentStock()).isEqualByComparingTo(BigDecimal.valueOf(7));
        verify(ingredientRepository, times(1)).save(ingredient);
    }

    @Test
    @DisplayName("pickUpIngredients() выбрасывает исключение, если ингредиент не найден")
    void pickUpIngredients_notFound_throwsException() {
        HashMap<Long, Integer> request = new HashMap<>();
        request.put(42L, 1);

        given(ingredientRepository.findById(42L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> ingredientService.pickUpIngredients(request))
                .isInstanceOf(IngredientNotFoundException.class);
    }
}
