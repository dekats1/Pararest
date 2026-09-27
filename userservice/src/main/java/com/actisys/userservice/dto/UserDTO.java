package com.actisys.userservice.dto;

import com.actisys.userservice.model.UserRole;
import lombok.*;

import javax.management.relation.Role;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private UUID id;
    private String login;
    private String fullName;
    private String email;
    private String phone;
    private LocalDate birthday;
    private BigDecimal wallet;
    private UserRole role;
    private Integer roomNumber;
    private Boolean blocked;
}
