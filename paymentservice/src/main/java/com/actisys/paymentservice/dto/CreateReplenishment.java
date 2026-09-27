package com.actisys.paymentservice.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateReplenishment {
    private BigDecimal totalAmount;
}
