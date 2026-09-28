package com.actisys.inventoryservice.service.impl;

import com.actisys.inventoryservice.dto.AddSupplyDTO;
import com.actisys.inventoryservice.dto.AddSupplyItemDTO;
import com.actisys.inventoryservice.dto.SupplyDTO;
import com.actisys.inventoryservice.exception.IngredientNotFoundException;
import com.actisys.inventoryservice.exception.SupplierNotFoundException;
import com.actisys.inventoryservice.exception.SupplyNotFoundException;
import com.actisys.inventoryservice.mapper.SupplyMapper;
import com.actisys.inventoryservice.model.Ingredient;
import com.actisys.inventoryservice.model.Metrics;
import com.actisys.inventoryservice.model.Supplier;
import com.actisys.inventoryservice.model.Supply;
import com.actisys.inventoryservice.model.SupplyItem;
import com.actisys.inventoryservice.model.SupplyStatus;
import com.actisys.inventoryservice.repository.IngredientRepository;
import com.actisys.inventoryservice.repository.SupplierRepository;
import com.actisys.inventoryservice.repository.SupplyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
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
class SupplyServiceImplTest {

    @Mock
    private SupplyRepository supplyRepository;

    @Mock
    private SupplyMapper supplyMapper;

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private IngredientRepository ingredientRepository;

    @InjectMocks
    private SupplyServiceImpl supplyService;

    private Supplier supplier;
    private Ingredient ingredient;
    private Supply supply;
    private SupplyDTO supplyDTO;

    @BeforeEach
    void setUp() {
        supplier = Supplier.builder().id(1L).name("ACME").build();
        ingredient = Ingredient.builder()
                .id(1L)
                .name("Flour")
                .unit(Metrics.KG)
                .currentStock(BigDecimal.valueOf(5))
                .build();
        supply = Supply.builder()
                .id(1L)
                .supplier(supplier)
                .status(SupplyStatus.IN_SORT_CENTER)
                .items(new ArrayList<>())
                .totalAmount(BigDecimal.ZERO)
                .build();
        supplyDTO = SupplyDTO.builder().id(1L).build();
    }

    @Test
    @DisplayName("getAll() возвращает список поставок")
    void getAll_returnsSupplies() {
        given(supplyRepository.findAll()).willReturn(List.of(supply));
        given(supplyMapper.toDTO(supply)).willReturn(supplyDTO);

        List<SupplyDTO> result = supplyService.getAll();

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("addNewSupply() создаёт поставку и считает итоговую сумму")
    void addNewSupply_createsSupplyWithTotalAmount() {
        AddSupplyItemDTO itemDTO = AddSupplyItemDTO.builder()
                .ingredientId(1L)
                .quantity(BigDecimal.valueOf(2))
                .priceAtSupply(BigDecimal.valueOf(10))
                .build();
        AddSupplyDTO addSupplyDTO = AddSupplyDTO.builder()
                .supplierId(1L)
                .items(List.of(itemDTO))
                .build();

        given(supplierRepository.findById(1L)).willReturn(Optional.of(supplier));
        given(ingredientRepository.findById(1L)).willReturn(Optional.of(ingredient));
        given(supplyRepository.save(any(Supply.class))).willAnswer(inv -> inv.getArgument(0));
        given(supplyMapper.toDTO(any(Supply.class))).willReturn(supplyDTO);

        SupplyDTO result = supplyService.addNewSupply(addSupplyDTO);

        assertThat(result).isNotNull();
        verify(supplyRepository, times(1)).save(any(Supply.class));
    }

    @Test
    @DisplayName("addNewSupply() выбрасывает исключение, если поставщик не найден")
    void addNewSupply_supplierNotFound_throwsException() {
        AddSupplyDTO addSupplyDTO = AddSupplyDTO.builder().supplierId(99L).items(List.of()).build();

        given(supplierRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> supplyService.addNewSupply(addSupplyDTO))
                .isInstanceOf(SupplierNotFoundException.class);

        verify(supplyRepository, never()).save(any());
    }

    @Test
    @DisplayName("addNewSupply() выбрасывает исключение, если ингредиент не найден")
    void addNewSupply_ingredientNotFound_throwsException() {
        AddSupplyItemDTO itemDTO = AddSupplyItemDTO.builder()
                .ingredientId(99L)
                .quantity(BigDecimal.ONE)
                .priceAtSupply(BigDecimal.TEN)
                .build();
        AddSupplyDTO addSupplyDTO = AddSupplyDTO.builder()
                .supplierId(1L)
                .items(List.of(itemDTO))
                .build();

        given(supplierRepository.findById(1L)).willReturn(Optional.of(supplier));
        given(ingredientRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> supplyService.addNewSupply(addSupplyDTO))
                .isInstanceOf(IngredientNotFoundException.class);
    }

    @Test
    @DisplayName("editSupply() при статусе SHIPPED увеличивает остаток ингредиентов")
    void editSupply_toShipped_increasesIngredientStock() {
        SupplyItem item = SupplyItem.builder()
                .ingredient(ingredient)
                .quantity(BigDecimal.valueOf(3))
                .priceAtSupply(BigDecimal.TEN)
                .build();
        supply.getItems().add(item);

        given(supplyRepository.findById(1L)).willReturn(Optional.of(supply));
        given(ingredientRepository.save(any(Ingredient.class))).willReturn(ingredient);
        given(supplyRepository.save(any(Supply.class))).willReturn(supply);
        given(supplyMapper.toDTO(supply)).willReturn(supplyDTO);

        SupplyDTO result = supplyService.editSupply(1L, SupplyStatus.SHIPPED);

        assertThat(result).isNotNull();
        assertThat(ingredient.getCurrentStock()).isEqualByComparingTo(BigDecimal.valueOf(8));
        assertThat(supply.getStatus()).isEqualTo(SupplyStatus.SHIPPED);
    }

    @Test
    @DisplayName("editSupply() выбрасывает исключение, если поставка не найдена")
    void editSupply_notFound_throwsException() {
        given(supplyRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> supplyService.editSupply(99L, SupplyStatus.SHIPPED))
                .isInstanceOf(SupplyNotFoundException.class);
    }

    @Test
    @DisplayName("editSupply() выбрасывает исключение, если поставка уже доставлена")
    void editSupply_alreadyShipped_throwsException() {
        supply.setStatus(SupplyStatus.SHIPPED);
        given(supplyRepository.findById(1L)).willReturn(Optional.of(supply));

        assertThatThrownBy(() -> supplyService.editSupply(1L, SupplyStatus.CANCELED))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("deleteSupply() удаляет поставку, если она ещё не доставлена")
    void deleteSupply_notShipped_deletesSupply() {
        given(supplyRepository.findById(1L)).willReturn(Optional.of(supply));

        supplyService.deleteSupply(1L);

        verify(supplyRepository, times(1)).delete(supply);
    }

    @Test
    @DisplayName("deleteSupply() выбрасывает исключение, если поставка уже доставлена")
    void deleteSupply_shipped_throwsException() {
        supply.setStatus(SupplyStatus.SHIPPED);
        given(supplyRepository.findById(1L)).willReturn(Optional.of(supply));

        assertThatThrownBy(() -> supplyService.deleteSupply(1L))
                .isInstanceOf(IllegalStateException.class);

        verify(supplyRepository, never()).delete(any());
    }

    @Test
    @DisplayName("deleteSupply() выбрасывает исключение, если поставка не найдена")
    void deleteSupply_notFound_throwsException() {
        given(supplyRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> supplyService.deleteSupply(99L))
                .isInstanceOf(SupplyNotFoundException.class);
    }

    @Test
    @DisplayName("getTotalAmount() суммирует стоимость всех поставок")
    void getTotalAmount_sumsAllSupplies() {
        Supply supply2 = Supply.builder().id(2L).totalAmount(BigDecimal.valueOf(50)).build();
        supply.setTotalAmount(BigDecimal.valueOf(30));

        given(supplyRepository.findAll()).willReturn(List.of(supply, supply2));

        BigDecimal total = supplyService.getTotalAmount();

        assertThat(total).isEqualByComparingTo(BigDecimal.valueOf(80));
    }
}
