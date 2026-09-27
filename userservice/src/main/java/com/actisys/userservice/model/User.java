package com.actisys.userservice.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User {

    @Id
    private UUID id;

    @Column(name = "login", unique = true,nullable = false)
    private String login;

    @Column(name = "full_name", nullable = true)
    private String fullName;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "phone", nullable = true, unique = true)
    private String phone;

    @Column(name = "birthday")
    private LocalDate birthday;

    @Column(name = "wallet", nullable = false)
    private BigDecimal wallet;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private UserRole role;

    @Column(name = "room_number", nullable = true)
    private Integer roomNumber;

    @Column(name = "is_blocked", nullable = false)
    private Boolean blocked;
}
