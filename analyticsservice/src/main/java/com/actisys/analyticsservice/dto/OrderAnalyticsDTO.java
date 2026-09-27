package com.actisys.analyticsservice.dto;

import lombok.Data;

import java.util.List;

@Data
public class OrderAnalyticsDTO {
    private String status;
    private String paymentStatus;
    private List<OrderInfoAnalyticsDTO> items;
}
