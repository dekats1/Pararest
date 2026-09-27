package com.actisys.inventoryservice.service.impl;

import com.actisys.inventoryservice.dto.AddSupplyDTO;
import com.actisys.inventoryservice.dto.AddSupplyItemDTO;
import com.actisys.inventoryservice.dto.SupplyDTO;
import com.actisys.inventoryservice.mapper.SupplyMapper;
import com.actisys.inventoryservice.model.Ingredient;
import com.actisys.inventoryservice.model.Supplier;
import com.actisys.inventoryservice.model.Supply;
import com.actisys.inventoryservice.repository.IngredientRepository;
import com.actisys.inventoryservice.repository.SupplierRepository;
import com.actisys.inventoryservice.repository.SupplyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

    @Test
    void addNewSupply_calculatesTotalAmountFromItems() {
        AddSupplyDTO request = AddSupplyDTO.builder()
                .supplierId(1L)
                .items(List.of(
                        AddSupplyItemDTO.builder().ingredientId(10L).quantity(new BigDecimal("2")).priceAtSupply(new BigDecimal("3.5")).build(),
                        AddSupplyItemDTO.builder().ingredientId(11L).quantity(new BigDecimal("1")).priceAtSupply(new BigDecimal("4.0")).build()
                ))
                .build();
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(Supplier.builder().id(1L).name("S").build()));
        when(ingredientRepository.findById(10L)).thenReturn(Optional.of(Ingredient.builder().id(10L).build()));
        when(ingredientRepository.findById(11L)).thenReturn(Optional.of(Ingredient.builder().id(11L).build()));
        when(supplyRepository.save(any(Supply.class))).thenAnswer(i -> i.getArgument(0));
        when(supplyMapper.toDTO(any(Supply.class))).thenReturn(SupplyDTO.builder().id(5L).build());

        supplyService.addNewSupply(request);

        ArgumentCaptor<Supply> captor = ArgumentCaptor.forClass(Supply.class);
        verify(supplyRepository).save(captor.capture());
        assertEquals(new BigDecimal("11.0"), captor.getValue().getTotalAmount());
    }
}
