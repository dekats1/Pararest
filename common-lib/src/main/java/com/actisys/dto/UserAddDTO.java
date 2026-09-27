package com.actisys.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserAddDTO {
    private String login;
    private String email;
    private String fullName;
    private String phone;
    private LocalDate birthday;
}
