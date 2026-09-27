package com.actisys.kafkacommunication.order;

import com.actisys.enums.PaymentType;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreatePaymentEvent {
    private UUID userId;
    private BigDecimal totalAmount;
    private Long orderId;
}
