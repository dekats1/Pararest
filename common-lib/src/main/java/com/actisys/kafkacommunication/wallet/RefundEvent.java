package com.actisys.kafkacommunication.wallet;

import com.actisys.enums.PaymentStatus;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefundEvent {
    private Long paymentId;
    private PaymentStatus paymentStatus;
}
