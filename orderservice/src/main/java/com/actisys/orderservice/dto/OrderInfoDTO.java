package com.actisys.orderservice.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderInfoDTO {
    private Long id;
    private Long menuItemId;
    private Integer quantity;
}
