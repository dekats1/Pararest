package com.actisys.compositeservice.config.ServiceProperties;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "orderservice")
public class OrderServiceProperties {
    private String host;
    private Endpoints endpoints;

    @Getter
    @Setter
    public static class Endpoints {
        private String getOrdersByUserId;
        private String getCart;
    }
}
