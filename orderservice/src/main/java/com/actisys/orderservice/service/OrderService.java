package com.actisys.orderservice.service;

import com.actisys.kafkacommunication.order.CreateOrderEvent;
import com.actisys.orderservice.dto.CreateOrderDTO;
import com.actisys.orderservice.dto.OrderDTO;

import java.util.List;
import java.util.UUID;

public interface OrderService {
    List<OrderDTO> getAllOrders();

    OrderDTO getOrderById(Long id);

    OrderDTO createNewOrder(UUID userId, CreateOrderDTO createOrderDTO);

    void updatePaymentStatus(CreateOrderEvent event);

    void deleteOrder(Long id);

    List<OrderDTO> getAllUserOrders(UUID id);

    void deleteDishFromCart(Long id, UUID uuid);

    void addDishToCart(Long id, UUID uuid);

    void clearCart(UUID uuid);

    OrderDTO getCart(UUID uuid);

    void cancelOrder(Long orderId);

    void updateStatusForCancelledOrder(CreateOrderEvent event);

    List<OrderDTO> getUserOrders(UUID uuid);

    Long getCount();
}
