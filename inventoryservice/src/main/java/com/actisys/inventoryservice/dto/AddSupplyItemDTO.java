package com.actisys.inventoryservice.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddSupplyItemDTO {
    private Long ingredientId;
    private BigDecimal quantity;
    private BigDecimal priceAtSupply;
}
