package com.actisys.compositeservice.dto.order;

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
