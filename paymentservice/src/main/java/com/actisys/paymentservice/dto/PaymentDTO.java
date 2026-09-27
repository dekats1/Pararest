package com.actisys.paymentservice.dto;

import com.actisys.enums.PaymentStatus;
import com.actisys.enums.PaymentType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentDTO {
    private Long id;
    private UUID userId;
    private BigDecimal totalAmount;
    private Long orderId;
    private PaymentType paymentType;
    private PaymentStatus paymentStatus;
    private LocalDateTime paymentDate;
}
