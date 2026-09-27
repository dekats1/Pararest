package com.actisys.inventoryservice.dto;

import com.actisys.inventoryservice.model.Metrics;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IngredientDTO {
    private Long id;
    private String name;
    private Metrics unit;
    private BigDecimal currentStock;
    private BigDecimal minStock;
    private LocalDateTime updatedAt;
    private BigDecimal price;
}
