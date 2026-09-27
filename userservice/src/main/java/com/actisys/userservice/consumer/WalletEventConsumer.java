package com.actisys.userservice.consumer;

import com.actisys.kafkacommunication.wallet.CreateWalletEvent;
import com.actisys.kafkacommunication.wallet.RefundMoneyEvent;
import com.actisys.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class WalletEventConsumer {

    private final UserService userService;

    @KafkaListener(topics = "REPLENISHMENT_EVENT", groupId = "user-service-group",
            containerFactory = "createWalletListenerFactory")
    public void handleReplenishmentEvent(CreateWalletEvent event) {
        log.info("handleReplenishmentEvent");
        userService.topUp(event);
    }

    @KafkaListener(topics = {"PAYMENT_EVENT", "WITHDRAW_MONEY"}, groupId = "user-service-group",
            containerFactory = "createWalletListenerFactory")
    public void handlePaymentEvent(CreateWalletEvent event) {
        log.info("handlePaymentEvent/WITHDRAW_MONEY");
        userService.withdrawMoney(event);
    }

    @KafkaListener(topics = "REFUND_EVENT", groupId = "user-service-group",
            containerFactory = "createRefundListenerFactory")
    public void handleRefundEvent(RefundMoneyEvent event) {
        userService.refundMoneys(event);
    }

}
