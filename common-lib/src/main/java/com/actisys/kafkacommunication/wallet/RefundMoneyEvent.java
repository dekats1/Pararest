package com.actisys.kafkacommunication.wallet;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundMoneyEvent {
  private UUID userId;
  private Long paymentId;
  private BigDecimal amount;
}
