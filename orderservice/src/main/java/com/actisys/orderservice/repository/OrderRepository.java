package com.actisys.orderservice.repository;

import com.actisys.orderservice.model.Order;
import com.actisys.orderservice.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findAllByUserId(UUID id);

    Optional<Order> findByUserIdAndStatus(UUID userId, OrderStatus status);

    List<Order> findAllByUserIdAndStatusNot(UUID uuid, OrderStatus orderStatus);
}
