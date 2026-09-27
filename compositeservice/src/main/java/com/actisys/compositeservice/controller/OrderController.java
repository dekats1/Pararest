package com.actisys.compositeservice.controller;

import com.actisys.compositeservice.dto.order.FullOrderDTO;
import com.actisys.compositeservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/composite/orders")
public class OrderController {

    private final OrderService orderService;

    @GetMapping("/getFullOrders")
    public Mono<ResponseEntity<List<FullOrderDTO>>> getFullOrders(@RequestHeader("X-User-Id") String id) {
        return orderService.getFullOrders(id);
    }

    @GetMapping("/cart")
    public Mono<ResponseEntity<FullOrderDTO>> getCart(@RequestHeader("X-User-Id") String id) {
        return orderService.getCart(id);
    }
}
