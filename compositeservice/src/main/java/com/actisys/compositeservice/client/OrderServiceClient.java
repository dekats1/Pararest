package com.actisys.compositeservice.client;

import com.actisys.compositeservice.dto.order.OrderDTO;
import reactor.core.publisher.Mono;

import java.util.List;

public interface OrderServiceClient {
    Mono<List<OrderDTO>> getOrdersByUserId(String userId);

    Mono<OrderDTO> getCart(String userId);
}
