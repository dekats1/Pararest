package com.actisys.analyticsservice.configuration.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "menuservice")
@Getter
@Setter
public class MenuServiceProperties {
    private String host;
    private Endpoints endpoints = new Endpoints();

    @Getter
    @Setter
    public static class Endpoints {
        private String getAll;
    }
}
