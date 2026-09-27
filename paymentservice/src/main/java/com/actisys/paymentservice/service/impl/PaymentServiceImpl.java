package com.actisys.paymentservice.service.impl;

import com.actisys.enums.PaymentStatus;
import com.actisys.enums.PaymentType;
import com.actisys.kafkacommunication.order.CreateOrderEvent;
import com.actisys.kafkacommunication.order.CreatePaymentEvent;
import com.actisys.kafkacommunication.replenishment.CreateReplenishmentEvent;
import com.actisys.kafkacommunication.wallet.CreateWalletEvent;
import com.actisys.kafkacommunication.wallet.RefundEvent;
import com.actisys.kafkacommunication.wallet.RefundMoneyEvent;
import com.actisys.paymentservice.dto.CreateReplenishment;
import com.actisys.paymentservice.dto.PaymentDTO;
import com.actisys.paymentservice.exception.PaymentNotFoundException;
import com.actisys.paymentservice.mapper.PaymentMapper;
import com.actisys.paymentservice.model.Payment;
import com.actisys.paymentservice.repository.PaymentRepository;
import com.actisys.paymentservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public List<PaymentDTO> getAllPayments() {
        List<Payment> allPayments = paymentRepository.findAll();
        return allPayments.stream().map(paymentMapper::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<PaymentDTO> getUserPayments(UUID uuid) {
        List<Payment> allUserPayments = paymentRepository.findAllByUserIdOrderByPaymentDateDesc(uuid);
        return allUserPayments.stream().map(paymentMapper::toDTO).collect(Collectors.toList());
    }

    @Override
    public PaymentDTO createNewReplenishment(CreateReplenishment createReplenishment, UUID uuid) {
        Payment payment = Payment.builder()
                .paymentDate(LocalDateTime.now())
                .paymentStatus(PaymentStatus.IN_WAIT)
                .paymentType(PaymentType.REPLENISHMENT)
                .orderId(0L)
                .totalAmount(createReplenishment.getTotalAmount())
                .userId(uuid)
                .build();
        Payment savedPayment = paymentRepository.save(payment);
        CreateWalletEvent walletEvent = CreateWalletEvent.builder()
                .userId(uuid)
                .paymentId(savedPayment.getId())
                .totalAmount(savedPayment.getTotalAmount())
                .build();
        kafkaTemplate.send("REPLENISHMENT_EVENT", walletEvent);
        return paymentMapper.toDTO(savedPayment);
    }

    @Override
    public void changePaymentStatus(CreateReplenishmentEvent event) {
        Payment payment = paymentRepository.findById(event.getPaymentId()).orElseThrow(PaymentNotFoundException::new);
        payment.setPaymentStatus(event.getPaymentStatus());
        paymentRepository.save(payment);
        if(payment.getPaymentStatus().equals(PaymentStatus.NOT_ENOUGH_MONEY) ||
                payment.getPaymentStatus().equals(PaymentStatus.ERROR)) {
            CreateOrderEvent orderEvent = CreateOrderEvent.builder()
                    .orderId(payment.getOrderId())
                    .paymentId(payment.getId())
                    .paymentStatus(event.getPaymentStatus())
                    .build();
            kafkaTemplate.send("CREATE_PAYMENT_ORDER_EVENT", orderEvent);
        }
        if(payment.getPaymentStatus().equals(PaymentStatus.PAID)) {
            CreateOrderEvent orderEvent = CreateOrderEvent.builder()
                    .orderId(payment.getOrderId())
                    .paymentId(payment.getId())
                    .paymentStatus(event.getPaymentStatus())
                    .build();
            kafkaTemplate.send("CREATE_PAYMENT_ORDER_EVENT", orderEvent);
        }
    }

    @Override
    public void createNewPayment(CreatePaymentEvent event) {
        Payment payment = Payment.builder()
                .paymentDate(LocalDateTime.now())
                .paymentStatus(PaymentStatus.IN_WAIT)
                .paymentType(PaymentType.CARD)
                .orderId(event.getOrderId())
                .totalAmount(event.getTotalAmount())
                .userId(event.getUserId())
                .build();
        Payment savedPayment = paymentRepository.save(payment);
        
        CreateWalletEvent walletEvent = CreateWalletEvent.builder()
                .userId(savedPayment.getUserId())
                .paymentId(savedPayment.getId())
                .totalAmount(savedPayment.getTotalAmount())
                .build();
        kafkaTemplate.send("WITHDRAW_MONEY", walletEvent);
        
        CreatePaymentEvent paymentEvent = CreatePaymentEvent.builder()
                .userId(savedPayment.getUserId())
                .totalAmount(savedPayment.getTotalAmount())
                .orderId(savedPayment.getOrderId())
                .build();
        kafkaTemplate.send("PAYMENT_EVENT", paymentEvent);
    }

    @Override
    public void cancelOrder(CreatePaymentEvent event) {
        Payment payment = paymentRepository.findByOrderId(event.getOrderId()).orElseThrow(PaymentNotFoundException::new);
        RefundMoneyEvent refundEvent = RefundMoneyEvent.builder()
                .amount(event.getTotalAmount())
                .paymentId(payment.getId())
                .userId(event.getUserId())
                .build();
        kafkaTemplate.send("REFUND_EVENT", refundEvent);
    }

    @Override
    public void refundMoney(RefundEvent event) {
        Payment payment = paymentRepository.findById(event.getPaymentId()).orElseThrow(PaymentNotFoundException::new);
        payment.setPaymentStatus(event.getPaymentStatus());
        paymentRepository.save(payment);
        CreateOrderEvent ordEvent = CreateOrderEvent.builder()
                .orderId(payment.getOrderId())
                .paymentId(payment.getId())
                .paymentStatus(event.getPaymentStatus())
                .build();
        kafkaTemplate.send("REFUND_EVENT_END", ordEvent);
    }

    @Override
    public BigDecimal getTotalAmount() {
        List<Payment> allPayments = paymentRepository.findAll();
        return allPayments.stream().map(Payment::getTotalAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public BigDecimal getRevenueAmount() {
        List<Payment> allPayments = paymentRepository.findAll();
        return allPayments.stream()
                .filter(payment -> payment.getPaymentStatus() == PaymentStatus.SUCCESS)
                .filter(payment -> payment.getPaymentType() == PaymentType.REPLENISHMENT
                        || payment.getPaymentType() == PaymentType.CASH)
                .map(Payment::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public Long getRevenueOperationsCount() {
        List<Payment> allPayments = paymentRepository.findAll();
        return allPayments.stream()
                .filter(payment -> payment.getPaymentStatus() == PaymentStatus.SUCCESS)
                .filter(payment -> payment.getPaymentType() == PaymentType.REPLENISHMENT
                        || payment.getPaymentType() == PaymentType.CASH)
                .count();
    }
}
