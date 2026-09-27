package com.actisys.inventoryservice.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "ingredients")
public class Ingredient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "unit", nullable = false)
    private Metrics unit;

    @Column(name = "current_stock")
    private BigDecimal currentStock;

    @Column(name = "min_stock")
    private BigDecimal minStock;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    private BigDecimal price;
}
