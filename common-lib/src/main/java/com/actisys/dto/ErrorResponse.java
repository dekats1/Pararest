package com.actisys.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor(force = true)
public class ErrorResponse {
    private String errorM;
    private String errorMessage;
}
