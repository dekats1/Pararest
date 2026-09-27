package com.actisys.kafkacommunication.wallet;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class CreateWalletEvent {
    private UUID userId;
    private Long paymentId;
    private BigDecimal totalAmount;
}
