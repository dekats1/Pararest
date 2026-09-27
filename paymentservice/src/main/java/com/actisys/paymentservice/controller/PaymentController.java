package com.actisys.paymentservice.controller;

import com.actisys.paymentservice.dto.CreateReplenishment;
import com.actisys.paymentservice.dto.PaymentDTO;
import com.actisys.paymentservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping("/getAllPayments")
    public ResponseEntity<List<PaymentDTO>> getAllPayments() {
        return ResponseEntity.ok().body(paymentService.getAllPayments());
    }

    @GetMapping("/getUserPayments")
    public ResponseEntity<List<PaymentDTO>> getUserPayments(@RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok().body(paymentService.getUserPayments(UUID.fromString(userId)));
    }

    @PostMapping("/newReplenishment")
    public ResponseEntity<PaymentDTO> newReplenishment(@RequestBody CreateReplenishment createReplenishment,
                                                       @RequestHeader("X-User-Id") String id) {
        return ResponseEntity.ok().body(paymentService.createNewReplenishment(createReplenishment, UUID.fromString(id)));
    }

    @GetMapping("/totalAmount")
    public ResponseEntity<BigDecimal> getTotalAmount() {
        return ResponseEntity.ok().body(paymentService.getTotalAmount());
    }

    @GetMapping("/revenueAmount")
    public ResponseEntity<BigDecimal> getRevenueAmount() {
        return ResponseEntity.ok().body(paymentService.getRevenueAmount());
    }

    @GetMapping("/revenueOperationsCount")
    public ResponseEntity<Long> getRevenueOperationsCount() {
        return ResponseEntity.ok().body(paymentService.getRevenueOperationsCount());
    }

}
