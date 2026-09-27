package com.actisys.analyticsservice.client;

import reactor.core.publisher.Mono;

import java.math.BigDecimal;

public interface InventoryServiceClient {
    Mono<BigDecimal> getTotalSuppliesAmount();
}
