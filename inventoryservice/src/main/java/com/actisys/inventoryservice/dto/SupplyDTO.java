package com.actisys.inventoryservice.dto;

import com.actisys.inventoryservice.model.SupplyStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupplyDTO {
    private Long id;
    private SupplierDTO supplier;
    private LocalDateTime dateOfRequest;
    private LocalDateTime dateOfSupply;
    private SupplyStatus status;
    private List<SupplyItemDTO> items;
    private BigDecimal totalPrice;
}
