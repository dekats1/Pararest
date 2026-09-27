package com.actisys.menuservice.dto;

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
    private Boolean active;
    private String adderName;
}
