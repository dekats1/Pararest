package com.actisys.inventoryservice.service;

import com.actisys.inventoryservice.dto.AddSupplyDTO;
import com.actisys.inventoryservice.dto.SupplyDTO;
import com.actisys.inventoryservice.model.SupplyStatus;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.util.List;

public interface SupplyService {
    List<SupplyDTO> getAll();

    SupplyDTO addNewSupply(AddSupplyDTO supplyDTO);

    SupplyDTO editSupply(Long id, SupplyStatus supplyStatus);

    void deleteSupply(Long id);

    BigDecimal getTotalAmount();
}
