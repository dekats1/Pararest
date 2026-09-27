package com.actisys.menuservice.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FullMenuItemDTO {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private CategoryDTO category;
    private Boolean active;
    private String imageUrl;
    private Integer preparationTime;
    private LocalDateTime updatedAt;
    private String adderName;
}
