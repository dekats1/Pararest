package com.actisys.userservice.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserProfileDTO {
    private String email;
    private String phone;
    private Integer roomNumber;
}
