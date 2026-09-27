package com.actisys.kafkacommunication.replenishment;

import com.actisys.enums.PaymentStatus;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateReplenishmentEvent {
    private Long paymentId;
    private PaymentStatus paymentStatus;
}
