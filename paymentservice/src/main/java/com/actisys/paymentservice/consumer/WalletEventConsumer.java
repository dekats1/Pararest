package com.actisys.paymentservice.consumer;

import com.actisys.kafkacommunication.order.CreatePaymentEvent;
import com.actisys.kafkacommunication.replenishment.CreateReplenishmentEvent;
import com.actisys.kafkacommunication.wallet.RefundEvent;
import com.actisys.kafkacommunication.wallet.WithdrawEvent;
import com.actisys.paymentservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WalletEventConsumer {

    private final PaymentService paymentService;

    @KafkaListener(topics = {"REPLENISHMENT_STATUS", "WITHDRAW_MONEY"}, groupId = "payment-service-group",
            containerFactory = "createReplenishmentListenerFactory")
    public void handleNewReplenishmentStatus(CreateReplenishmentEvent event) {
        paymentService.changePaymentStatus(event);
    }

    @KafkaListener(topics = "CREATE_ORDER_CANCEL_EVENT", groupId = "payment-service-group",
            containerFactory = "createPaymentListenerFactory")
    public void handleRefundMoneys(CreatePaymentEvent event){
        paymentService.cancelOrder(event);
    }

    @KafkaListener(topics = "REFUND_STATUS_EVENT", groupId = "payment-service-group",
            containerFactory = "refundListenerFactory")
    public void handleRefundMoney(RefundEvent event){
        paymentService.refundMoney(event);
    }
}
