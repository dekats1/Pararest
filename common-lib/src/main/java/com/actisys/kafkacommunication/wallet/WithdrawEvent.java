package com.actisys.kafkacommunication.wallet;
import com.actisys.enums.PaymentStatus;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WithdrawEvent {
    private Long paymentId;
    private PaymentStatus paymentStatus;
}
