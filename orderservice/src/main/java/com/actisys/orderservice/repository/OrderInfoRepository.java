package com.actisys.orderservice.repository;

import com.actisys.orderservice.model.Order;
import com.actisys.orderservice.model.OrderInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderInfoRepository extends JpaRepository<OrderInfo, Long> {
    Optional<OrderInfo> findByOrderAndMenuItemId(Order order, Long id);
}
