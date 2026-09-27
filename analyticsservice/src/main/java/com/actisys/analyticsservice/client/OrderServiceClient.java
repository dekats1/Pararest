package com.actisys.analyticsservice.client;

import com.actisys.analyticsservice.dto.OrderAnalyticsDTO;
import reactor.core.publisher.Mono;

import java.util.List;

public interface OrderServiceClient {
    Mono<Long> getTotalCount();

    Mono<List<OrderAnalyticsDTO>> getAllOrders();
}