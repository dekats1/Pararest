package com.actisys.orderservice.dto;

import com.actisys.enums.PaymentStatus;
import com.actisys.enums.PaymentType;
import com.actisys.orderservice.model.OrderStatus;
import com.actisys.orderservice.model.OrderType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDTO {
    private Long id;
    private List<OrderInfoDTO> items;
    private UUID userId;
    private OrderStatus status;
    private OrderType type;
    private LocalDateTime orderTime;
    private LocalDateTime deliveryTime;
    private BigDecimal totalPrice;
    private PaymentStatus paymentStatus;
    private PaymentType paymentType;
    private String userRequest;
}
