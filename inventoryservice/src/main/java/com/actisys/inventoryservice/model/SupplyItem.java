package com.actisys.inventoryservice.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class SupplyItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "supply_id")
    private Supply supply;

    @ManyToOne
    private Ingredient ingredient;

    private BigDecimal quantity;
    private BigDecimal priceAtSupply;
}
