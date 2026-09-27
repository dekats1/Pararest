package com.actisys.compositeservice.dto.order;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FullOrderDTO {
    private OrderDTO order;
    private List<MenuItemDTO> menuItem;
}
