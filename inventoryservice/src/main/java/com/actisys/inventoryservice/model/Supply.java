package com.actisys.inventoryservice.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "supply")
public class Supply {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @Column(name = "date_of_request", nullable = false)
    private LocalDateTime dateOfRequest;

    @Column(name = "date_of_supply")
    private LocalDateTime dateOfSupply;

    @Enumerated(EnumType.STRING)
    private SupplyStatus status;

    @OneToMany(mappedBy = "supply", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SupplyItem> items = new ArrayList<>();

    private BigDecimal totalAmount;

}

