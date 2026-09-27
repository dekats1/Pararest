package com.actisys.orderservice.service.impl;

import com.actisys.enums.PaymentType;
import com.actisys.kafkacommunication.order.CreateOrderEvent;
import com.actisys.orderservice.dto.CreateOrderDTO;
import com.actisys.orderservice.dto.OrderDTO;
import com.actisys.orderservice.exception.CanNotCancelOrderNowException;
import com.actisys.orderservice.mapper.OrderMapper;
import com.actisys.orderservice.model.Order;
import com.actisys.orderservice.model.OrderInfo;
import com.actisys.orderservice.model.OrderStatus;
import com.actisys.orderservice.model.OrderType;
import com.actisys.orderservice.repository.OrderInfoRepository;
import com.actisys.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderMapper orderMapper;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderInfoRepository orderInfoRepository;
    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private OrderServiceImpl orderService;

    @Test
    void createNewOrder_withCardPayment_sendsPaymentEvent() {
        UUID userId = UUID.randomUUID();
        Order cart = Order.builder().id(7L).userId(userId).status(OrderStatus.IN_CART).build();
        CreateOrderDTO dto = CreateOrderDTO.builder()
                .orderType(OrderType.IN_RESTAURANT)
                .paymentType(PaymentType.CARD)
                .totalPrice(new BigDecimal("100.00"))
                .userRequest("No onions")
                .build();
        OrderDTO mapped = OrderDTO.builder().id(7L).build();

        when(orderRepository.findByUserIdAndStatus(userId, OrderStatus.IN_CART)).thenReturn(Optional.of(cart));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));
        when(orderMapper.toDTO(any(Order.class))).thenReturn(mapped);

        OrderDTO result = orderService.createNewOrder(userId, dto);

        assertEquals(7L, result.getId());
        verify(kafkaTemplate).send(eq("CREATE_ORDER_PAYMENT_EVENT"), any());
    }

    @Test
    void createNewOrder_withCashPayment_doesNotSendPaymentEvent() {
        UUID userId = UUID.randomUUID();
        Order cart = Order.builder().id(8L).userId(userId).status(OrderStatus.IN_CART).items(new ArrayList<>()).build();
        CreateOrderDTO dto = CreateOrderDTO.builder()
                .orderType(OrderType.IN_RESTAURANT)
                .paymentType(PaymentType.CASH)
                .totalPrice(new BigDecimal("250.00"))
                .userRequest("No sugar")
                .build();

        when(orderRepository.findByUserIdAndStatus(userId, OrderStatus.IN_CART)).thenReturn(Optional.of(cart));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));
        when(orderMapper.toDTO(any(Order.class))).thenReturn(OrderDTO.builder().id(8L).build());

        OrderDTO result = orderService.createNewOrder(userId, dto);

        assertEquals(8L, result.getId());
        verify(kafkaTemplate, never()).send(eq("CREATE_ORDER_PAYMENT_EVENT"), any());
    }

    @Test
    void updatePaymentStatus_withError_setsOrderDiscarded() {
        Order order = Order.builder().id(11L).status(OrderStatus.PENDING).build();
        CreateOrderEvent event = CreateOrderEvent.builder()
                .orderId(11L)
                .paymentStatus(com.actisys.enums.PaymentStatus.ERROR)
                .build();

        when(orderRepository.findById(11L)).thenReturn(Optional.of(order));

        orderService.updatePaymentStatus(event);

        assertEquals(OrderStatus.DISCARDED, order.getStatus());
        assertEquals(com.actisys.enums.PaymentStatus.ERROR, order.getPaymentStatus());
        verify(orderRepository).save(order);
    }

    @Test
    void addDishToCart_existingDish_incrementsQuantity() {
        UUID userId = UUID.randomUUID();
        Order cart = Order.builder().id(12L).userId(userId).status(OrderStatus.IN_CART).items(new ArrayList<>()).build();
        OrderInfo existingInfo = OrderInfo.builder().order(cart).menuItemId(5L).quantity(1).build();

        when(orderRepository.findByUserIdAndStatus(userId, OrderStatus.IN_CART)).thenReturn(Optional.of(cart));
        when(orderInfoRepository.findByOrderAndMenuItemId(cart, 5L)).thenReturn(Optional.of(existingInfo));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

        orderService.addDishToCart(5L, userId);

        assertEquals(2, existingInfo.getQuantity());
        verify(orderRepository).save(cart);
    }

    @Test
    void cancelOrder_sendsCancelEvent() {
        UUID userId = UUID.randomUUID();
        Order order = Order.builder()
                .id(13L)
                .userId(userId)
                .totalPrice(new BigDecimal("99.90"))
                .build();
        when(orderRepository.findById(13L)).thenReturn(Optional.of(order));

        orderService.cancelOrder(13L);

        verify(kafkaTemplate).send(eq("CREATE_ORDER_CANCEL_EVENT"), any());
    }

    @Test
    void updateStatusForCancelledOrder_withError_throwsException() {
        Order order = Order.builder().id(14L).status(OrderStatus.PENDING).build();
        CreateOrderEvent event = CreateOrderEvent.builder()
                .orderId(14L)
                .paymentStatus(com.actisys.enums.PaymentStatus.ERROR)
                .build();
        when(orderRepository.findById(14L)).thenReturn(Optional.of(order));

        assertThrows(CanNotCancelOrderNowException.class, () -> orderService.updateStatusForCancelledOrder(event));
    }
}
