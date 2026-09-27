package com.actisys.compositeservice.dto.menu;

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
