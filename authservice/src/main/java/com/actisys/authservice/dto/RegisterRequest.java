package com.actisys.authservice.dto;

import com.actisys.dto.UserAddDTO;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequest {
    private String password;
    private boolean rememberMe;
    private UserAddDTO newUser;
}
