package com.actisys.inventoryservice.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddSupplyDTO {
    private Long supplierId;
    private List<AddSupplyItemDTO> items;
}
