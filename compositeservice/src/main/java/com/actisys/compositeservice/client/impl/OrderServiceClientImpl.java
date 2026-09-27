package com.actisys.compositeservice.client.impl;

import com.actisys.compositeservice.client.OrderServiceClient;
import com.actisys.compositeservice.config.ServiceProperties.MenuServiceProperties;
import com.actisys.compositeservice.config.ServiceProperties.OrderServiceProperties;
import com.actisys.compositeservice.dto.order.OrderDTO;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

@Component
public class OrderServiceClientImpl implements OrderServiceClient {

    private final WebClient webClient;
    private final OrderServiceProperties orderProperties;

    public OrderServiceClientImpl(WebClient.Builder webClientBuilder, OrderServiceProperties orderProperties) {
        this.orderProperties = orderProperties;
        this.webClient = webClientBuilder
                .baseUrl(orderProperties.getHost())
                .build();
    }

    @Override
    public Mono<List<OrderDTO>> getOrdersByUserId(String userId) {
        return webClient.get()
                .uri(orderProperties.getHost() + orderProperties.getEndpoints().getGetOrdersByUserId() + userId)
                .retrieve()
                .bodyToFlux(OrderDTO.class)
                .collectList()
                .timeout(Duration.ofSeconds(10));
    }

    @Override
    public Mono<OrderDTO> getCart(String userId) {
        return webClient.get()
                .uri(orderProperties.getHost() + orderProperties.getEndpoints().getGetCart() + userId)
                .retrieve()
                .bodyToMono(OrderDTO.class)
                .timeout(Duration.ofSeconds(10));
    }
}
