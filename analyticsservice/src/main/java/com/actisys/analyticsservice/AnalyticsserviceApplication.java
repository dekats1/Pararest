package com.actisys.analyticsservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class AnalyticsserviceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AnalyticsserviceApplication.class, args);
    }

}
