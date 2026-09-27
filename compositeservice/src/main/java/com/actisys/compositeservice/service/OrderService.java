package com.actisys.compositeservice.service;

import com.actisys.compositeservice.dto.order.FullOrderDTO;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;

import java.util.List;

public interface OrderService {
    Mono<ResponseEntity<List<FullOrderDTO>>> getFullOrders(String id);
    Mono<ResponseEntity<FullOrderDTO>> getCart(String id);
}
