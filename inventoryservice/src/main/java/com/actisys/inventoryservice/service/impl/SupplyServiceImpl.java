package com.actisys.inventoryservice.service.impl;

import com.actisys.inventoryservice.dto.AddSupplyDTO;
import com.actisys.inventoryservice.dto.AddSupplyItemDTO;
import com.actisys.inventoryservice.dto.SupplyDTO;
import com.actisys.inventoryservice.exception.IngredientNotFoundException;
import com.actisys.inventoryservice.exception.SupplierNotFoundException;
import com.actisys.inventoryservice.exception.SupplyNotFoundException;
import com.actisys.inventoryservice.mapper.IngredientMapper;
import com.actisys.inventoryservice.mapper.SupplierMapper;
import com.actisys.inventoryservice.mapper.SupplyMapper;
import com.actisys.inventoryservice.model.Ingredient;
import com.actisys.inventoryservice.model.Supply;
import com.actisys.inventoryservice.model.SupplyItem;
import com.actisys.inventoryservice.model.SupplyStatus;
import com.actisys.inventoryservice.repository.IngredientRepository;
import com.actisys.inventoryservice.repository.SupplierRepository;
import com.actisys.inventoryservice.repository.SupplyRepository;
import com.actisys.inventoryservice.service.IngredientService;
import com.actisys.inventoryservice.service.SupplyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SupplyServiceImpl implements SupplyService {

    private final SupplyRepository supplyRepository;
    private final SupplyMapper supplyMapper;
    private final SupplierRepository supplierRepository;
    private final IngredientRepository ingredientRepository;

    @Override
    public List<SupplyDTO> getAll() {
        List<Supply> list =supplyRepository.findAll() ;
        return list.stream().map(supplyMapper::toDTO).collect(Collectors.toList());
    }

    @Override
    public SupplyDTO addNewSupply(AddSupplyDTO supplyDTO) {
        Supply supply = Supply.builder()
                .supplier(supplierRepository.findById(supplyDTO.getSupplierId()).orElseThrow(
                        ()-> new SupplierNotFoundException(supplyDTO.getSupplierId())))
                .dateOfRequest(LocalDateTime.now())
                .status(SupplyStatus.IN_SORT_CENTER)
                .items(new ArrayList<>())
                .build();
        for(AddSupplyItemDTO itemDTO : supplyDTO.getItems()){
            Ingredient ingredient = ingredientRepository.findById(itemDTO.getIngredientId()).orElseThrow(
                    ()->new IngredientNotFoundException(itemDTO.getIngredientId()));
            SupplyItem item = SupplyItem.builder()
                    .supply(supply)
                    .ingredient(ingredient)
                    .priceAtSupply(itemDTO.getPriceAtSupply())
                    .quantity(itemDTO.getQuantity())
                    .build();
            supply.getItems().add(item);
        }
        BigDecimal totalPrice = supply.getItems().stream()
                .map(item->item.getPriceAtSupply().multiply(item.getQuantity()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        supply.setTotalAmount(totalPrice);
        Supply savedSupply = supplyRepository.save(supply);
        return supplyMapper.toDTO(savedSupply);
    }

    @Override
    public SupplyDTO editSupply(Long id, SupplyStatus status) {
        Supply supply = supplyRepository.findById(id).orElseThrow(()->new SupplyNotFoundException(id));
        if(SupplyStatus.SHIPPED.equals(supply.getStatus())){
            throw new IllegalStateException("This supply already delivered");
        }

        if(status.equals(SupplyStatus.SHIPPED)){
            for(SupplyItem supplyItem : supply.getItems()){
                Ingredient ingredient = supplyItem.getIngredient();
                ingredient.setCurrentStock(ingredient.getCurrentStock().add(supplyItem.getQuantity()));
                ingredient.setUpdatedAt(LocalDateTime.now());
                ingredientRepository.save(ingredient);
            }
        }
        supply.setDateOfSupply(LocalDateTime.now());
        supply.setStatus(status);
        return supplyMapper.toDTO(supplyRepository.save(supply));
    }

    @Override
    public void deleteSupply(Long id) {
        Supply supply = supplyRepository.findById(id).orElseThrow(()->new SupplyNotFoundException(id));
        if(supply.getStatus().equals(SupplyStatus.SHIPPED)){
            throw new IllegalStateException("This supply already delivered");
        }
        supplyRepository.delete(supply);
    }

    @Override
    public BigDecimal getTotalAmount() {
        List<Supply> supplyList = supplyRepository.findAll();
        return supplyList.stream().map(Supply::getTotalAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
