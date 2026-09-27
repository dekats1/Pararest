package com.actisys.kafkacommunication.order;

import com.actisys.enums.PaymentStatus;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateOrderEvent {
    private Long orderId;
    private Long paymentId;
    private PaymentStatus paymentStatus;
}
