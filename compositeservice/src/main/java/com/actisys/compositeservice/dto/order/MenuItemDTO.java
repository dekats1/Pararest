package com.actisys.compositeservice.dto.order;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MenuItemDTO {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private CategoryDTO category;
    private String imageUrl;
    private Integer preparationTime;
}
