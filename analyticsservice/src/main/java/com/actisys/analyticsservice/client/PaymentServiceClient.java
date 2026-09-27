package com.actisys.analyticsservice.client;

import reactor.core.publisher.Mono;

import java.math.BigDecimal;

public interface PaymentServiceClient {
    Mono<BigDecimal> getTotalAmount();

    Mono<BigDecimal> getRevenueAmount();

    Mono<Long> getRevenueOperationsCount();
}