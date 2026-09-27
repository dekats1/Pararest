package com.actisys.staffservice.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "compensation_record")
public class CompensationRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Employee employee;

    @Enumerated(EnumType.STRING)
    private CompensationType type;

    private LocalDate periodStart;
    private LocalDate periodEnd;

    private BigDecimal baseAmount;
    private BigDecimal bonusAmount;
    private BigDecimal totalAmount;
    private Integer daysWorked;
    private Integer daysCompensated;
    private Integer hoursWorked;
    private LocalDate paymentDate;

    private String documentNumber;
    private LocalDate documentDate;
}
