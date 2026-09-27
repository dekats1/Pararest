package com.actisys.inventoryservice.dto;

import com.actisys.inventoryservice.model.Metrics;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AddIngredientDTO {
    private String name;
    private Metrics unit;
    private BigDecimal currentStock;
    private BigDecimal minStock;
}
