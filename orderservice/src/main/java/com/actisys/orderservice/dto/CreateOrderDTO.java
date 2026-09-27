package com.actisys.orderservice.dto;

import com.actisys.enums.PaymentType;
import com.actisys.orderservice.model.OrderType;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderDTO {
    private OrderType orderType;
    private BigDecimal totalPrice;
    private PaymentType paymentType;
    private String userRequest;
}
