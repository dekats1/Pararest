package com.actisys.orderservice.service.impl;

import com.actisys.dto.UpdatePaymentStatus;
import com.actisys.enums.PaymentStatus;
import com.actisys.enums.PaymentType;
import com.actisys.kafkacommunication.order.CreateOrderEvent;
import com.actisys.kafkacommunication.order.CreatePaymentEvent;
import com.actisys.orderservice.dto.CreateOrderDTO;
import com.actisys.orderservice.dto.OrderDTO;
import com.actisys.orderservice.exception.CanNotCancelOrderNowException;
import com.actisys.orderservice.exception.OrderInfoNotFoundException;
import com.actisys.orderservice.exception.OrderNotFoundException;
import com.actisys.orderservice.mapper.OrderMapper;
import com.actisys.orderservice.model.Order;
import com.actisys.orderservice.model.OrderInfo;
import com.actisys.orderservice.model.OrderStatus;
import com.actisys.orderservice.repository.OrderInfoRepository;
import com.actisys.orderservice.repository.OrderRepository;
import com.actisys.orderservice.service.OrderService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;
    private final OrderRepository orderRepository;
    private final OrderInfoRepository orderInfoRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public List<OrderDTO> getAllOrders() {
        List<Order> orders = orderRepository.findAll();
        return orders.stream().map(orderMapper::toDTO).collect(Collectors.toList());
    }

    @Override
    public OrderDTO getOrderById(Long id) {
        Order order = orderRepository.findById(id).orElseThrow(RuntimeException::new);
        return orderMapper.toDTO(order);
    }

    @Override
    public OrderDTO createNewOrder(UUID userId, CreateOrderDTO createOrderDTO) {
       Order order = orderRepository.findByUserIdAndStatus(userId, OrderStatus.IN_CART)
               .orElseThrow(OrderNotFoundException::new);
       order.setOrderTime(LocalDateTime.now());
       order.setStatus(OrderStatus.PENDING);
       order.setPaymentStatus(PaymentStatus.NOT_PAID);
       order.setPaymentType(createOrderDTO.getPaymentType());
       order.setTotalPrice(createOrderDTO.getTotalPrice());
       order.setType(createOrderDTO.getOrderType());
       order.setUserRequest(createOrderDTO.getUserRequest());
       Order savedOrder = orderRepository.save(order);
       if(savedOrder.getPaymentType().equals(PaymentType.CARD)) {
           CreatePaymentEvent paymentEvent = CreatePaymentEvent.builder()
                   .orderId(savedOrder.getId())
                   .totalAmount(savedOrder.getTotalPrice())
                   .userId(userId)
                   .build();
           kafkaTemplate.send("CREATE_ORDER_PAYMENT_EVENT", paymentEvent);
       }
       return orderMapper.toDTO(savedOrder);
    }

    @Override
    public void updatePaymentStatus(CreateOrderEvent event) {
        Order order = orderRepository.findById(event.getOrderId()).orElseThrow(OrderNotFoundException::new);
        order.setPaymentStatus(event.getPaymentStatus());
        if(order.getPaymentStatus().equals(PaymentStatus.ERROR) ||
            order.getPaymentStatus().equals(PaymentStatus.NOT_ENOUGH_MONEY)) {
            order.setStatus(OrderStatus.DISCARDED);
        }
        if(order.getPaymentStatus().equals(PaymentStatus.PAID)) {
            order.setStatus(OrderStatus.PENDING);
        }
        orderRepository.save(order);
    }

    @Override
    public void deleteOrder(Long id) {
        Order order = orderRepository.findById(id).orElseThrow(OrderNotFoundException::new);
        orderRepository.delete(order);
    }

    @Override
    public List<OrderDTO> getAllUserOrders(UUID id) {
        List<Order> allUserOrders = orderRepository.findAllByUserId(id);
        return allUserOrders.stream().map(orderMapper::toDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteDishFromCart(Long menuItemId, UUID userId) {
        Order order = orderRepository.findByUserIdAndStatus(userId, OrderStatus.IN_CART)
                .orElseThrow(OrderNotFoundException::new);
        OrderInfo orderInfo = orderInfoRepository.findByOrderAndMenuItemId(order, menuItemId)
                .orElseThrow(OrderInfoNotFoundException::new);
        order.getItems().remove(orderInfo);
        orderRepository.save(order);
    }

    @Override
    @Transactional
    public void addDishToCart(Long menuItemId, UUID userId) {
        Order order = orderRepository.findByUserIdAndStatus(userId, OrderStatus.IN_CART)
                .orElseGet(() -> {
                    Order newOrder = new Order();
                    newOrder.setUserId(userId);
                    newOrder.setStatus(OrderStatus.IN_CART);
                    return orderRepository.save(newOrder);
                });

        OrderInfo orderInfo = orderInfoRepository.findByOrderAndMenuItemId(order, menuItemId)
                .map(info -> {
                    info.setQuantity(info.getQuantity() + 1);
                    return info;
                })
                .orElseGet(() -> OrderInfo.builder()
                        .order(order)
                        .menuItemId(menuItemId)
                        .quantity(1)
                        .build());

        order.getItems().add(orderInfo);
        orderRepository.save(order);
    }


    @Override
    public void clearCart(UUID uuid) {
        Order order = orderRepository.findByUserIdAndStatus(uuid, OrderStatus.IN_CART)
                .orElseThrow(OrderNotFoundException::new);
        order.getItems().clear();
        orderRepository.save(order);
    }

    @Override
    public OrderDTO getCart(UUID uuid) {
        Order order = orderRepository.findByUserIdAndStatus(uuid, OrderStatus.IN_CART)
                .orElseThrow(OrderNotFoundException::new);
        return orderMapper.toDTO(order);
    }

    @Override
    public void cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(OrderNotFoundException::new);
        CreatePaymentEvent event = CreatePaymentEvent.builder()
                .totalAmount(order.getTotalPrice())
                .userId(order.getUserId())
                .orderId(orderId)
                .build();
        kafkaTemplate.send("CREATE_ORDER_CANCEL_EVENT", event);
    }

    @Override
    public void updateStatusForCancelledOrder(CreateOrderEvent event) {
        Order order = orderRepository.findById(event.getOrderId()).orElseThrow(OrderNotFoundException::new);
        if(event.getPaymentStatus().equals(PaymentStatus.SUCCESS)) {
            order.setPaymentStatus(PaymentStatus.REFUNDED);
            order.setStatus(OrderStatus.CANCELED);
            orderRepository.save(order);
        }
        if(event.getPaymentStatus().equals(PaymentStatus.ERROR)) {
            throw new CanNotCancelOrderNowException();
        }
    }

    @Override
    public List<OrderDTO> getUserOrders(UUID uuid) {
        List<Order> allOrders = orderRepository.findAllByUserIdAndStatusNot(uuid, OrderStatus.IN_CART);
        return allOrders.stream().map(orderMapper::toDTO).collect(Collectors.toList());
    }

    @Override
    public Long getCount() {
        return orderRepository.count();
    }
}
