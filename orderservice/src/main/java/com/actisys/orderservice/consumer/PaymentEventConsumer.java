package com.actisys.orderservice.consumer;

import com.actisys.kafkacommunication.order.CreateOrderEvent;
import com.actisys.kafkacommunication.wallet.RefundEvent;
import com.actisys.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentEventConsumer {

    private final OrderService orderService;

    @KafkaListener(topics = "CREATE_PAYMENT_ORDER_EVENT", groupId = "order-service-group",
            containerFactory = "createOrderListenerFactory")
    public void handlePaymentStatus(CreateOrderEvent event) {
        orderService.updatePaymentStatus(event);
    }

    @KafkaListener(topics = "REFUND_EVENT_END", groupId = "order-service-group",
            containerFactory = "createOrderListenerFactory")
    public void handleRefundStatus(CreateOrderEvent event) {
        orderService.updateStatusForCancelledOrder(event);
    }
}
