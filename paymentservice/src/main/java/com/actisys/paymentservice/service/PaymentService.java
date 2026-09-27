package com.actisys.paymentservice.service;

import com.actisys.kafkacommunication.order.CreatePaymentEvent;
import com.actisys.kafkacommunication.replenishment.CreateReplenishmentEvent;
import com.actisys.kafkacommunication.wallet.RefundEvent;
import com.actisys.paymentservice.dto.CreateReplenishment;
import com.actisys.paymentservice.dto.PaymentDTO;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface PaymentService {
    List<PaymentDTO> getAllPayments();

    List<PaymentDTO> getUserPayments(UUID uuid);

    PaymentDTO createNewReplenishment(CreateReplenishment createReplenishment, UUID uuid);

    void changePaymentStatus(CreateReplenishmentEvent event);

    void createNewPayment(CreatePaymentEvent event);

    void cancelOrder(CreatePaymentEvent event);

    void refundMoney(RefundEvent event);

    BigDecimal getTotalAmount();

    BigDecimal getRevenueAmount();

    Long getRevenueOperationsCount();
}
