package com.actisys.inventoryservice.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupplyItemDTO {
    private Long id;
    private SupplyDTO supply;
    private IngredientDTO ingredient;
    private BigDecimal quantity;
    private BigDecimal priceAtSupply;
}
