package com.actisys.staffservice.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "employee")
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "hire_date")
    private LocalDate hireDate;

    @Column(name = "termination_date")
    private LocalDate terminationDate;

    @Column(name = "base_salary")
    private BigDecimal baseSalary;

    @Enumerated(EnumType.STRING)
    private EmployeeStatus status;

    @OneToMany(mappedBy = "employee")
    private List<WorkSchedule> workSchedules;

    @OneToMany(mappedBy = "employee")
    private List<CompensationRecord> compensations;
}
