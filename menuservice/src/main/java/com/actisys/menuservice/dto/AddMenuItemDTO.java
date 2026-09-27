package com.actisys.menuservice.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AddMenuItemDTO {
    private String name;
    private String description;
    private BigDecimal price;
    private MenuCategoryDTO category;
    private String imageUrl;
    private Integer preparationTime;
}
