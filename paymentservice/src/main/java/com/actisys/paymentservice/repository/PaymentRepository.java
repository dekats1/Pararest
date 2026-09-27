package com.actisys.paymentservice.repository;

import com.actisys.paymentservice.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findAllByUserIdOrderByPaymentDateDesc(UUID uuid);

    Optional<Payment> findByOrderId(Long orderId);
}
