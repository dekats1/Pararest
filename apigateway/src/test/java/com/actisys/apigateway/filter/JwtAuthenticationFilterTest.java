package com.actisys.apigateway.filter;

import com.actisys.apigateway.util.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private GatewayFilterChain chain;

    @Test
    void filter_whenProtectedPathWithoutToken_returnsUnauthorized() {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtTokenProvider);
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/orders").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        filter.filter(exchange, chain).block();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        verify(jwtTokenProvider, never()).verify(org.mockito.ArgumentMatchers.anyString());
    }
}
