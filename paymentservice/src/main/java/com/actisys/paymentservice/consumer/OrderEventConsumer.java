package com.actisys.paymentservice.consumer;

import com.actisys.kafkacommunication.order.CreatePaymentEvent;
import com.actisys.paymentservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderEventConsumer {

    private final PaymentService paymentService;

    @KafkaListener(topics = "CREATE_ORDER_PAYMENT_EVENT", groupId = "payment-service-group",
            containerFactory = "createPaymentListenerFactory")
    public void handleCreateNewOrder(CreatePaymentEvent event) {
        paymentService.createNewPayment(event);
    }

}
