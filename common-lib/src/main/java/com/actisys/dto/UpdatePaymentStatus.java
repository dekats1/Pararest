package com.actisys.dto;

import com.actisys.enums.PaymentStatus;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePaymentStatus {
    private Long paymentId;
    private Long orderId;
    private PaymentStatus paymentStatus;
}
