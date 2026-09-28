package com.actisys.inventoryservice.repository;

import com.actisys.inventoryservice.model.Ingredient;
import com.actisys.inventoryservice.model.Metrics;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;


@DataJpaTest
class IngredientRepositoryIT {

    @Autowired
    private IngredientRepository ingredientRepository;

    @Test
    @DisplayName("Сохранение и поиск ингредиента по id")
    void saveAndFindById() {
        Ingredient ingredient = Ingredient.builder()
                .name("Flour")
                .unit(Metrics.KG)
                .currentStock(BigDecimal.TEN)
                .minStock(BigDecimal.ONE)
                .updatedAt(LocalDateTime.now())
                .build();

        Ingredient saved = ingredientRepository.save(ingredient);

        Optional<Ingredient> found = ingredientRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Flour");
    }

    @Test
    @DisplayName("findAll() возвращает все сохранённые ингредиенты")
    void findAll_returnsAllIngredients() {
        ingredientRepository.save(Ingredient.builder().name("Flour").unit(Metrics.KG)
                .currentStock(BigDecimal.TEN).minStock(BigDecimal.ONE).build());
        ingredientRepository.save(Ingredient.builder().name("Sugar").unit(Metrics.KG)
                .currentStock(BigDecimal.valueOf(5)).minStock(BigDecimal.ONE).build());

        List<Ingredient> all = ingredientRepository.findAll();

        assertThat(all).hasSize(2);
    }

    @Test
    @DisplayName("delete() удаляет ингредиент")
    void delete_removesIngredient() {
        Ingredient saved = ingredientRepository.save(Ingredient.builder()
                .name("Flour").unit(Metrics.KG)
                .currentStock(BigDecimal.TEN).minStock(BigDecimal.ONE).build());

        ingredientRepository.delete(saved);

        assertThat(ingredientRepository.findById(saved.getId())).isEmpty();
    }
}
