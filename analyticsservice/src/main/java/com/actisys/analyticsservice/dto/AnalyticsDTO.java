package com.actisys.analyticsservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsDTO {
    private BigDecimal totalRevenue;
    private BigDecimal totalExpenses;
    private BigDecimal netProfit;
    private Long totalOrders;
    private BigDecimal averageCheck;
    private String topDishName;
    private Long topDishCount;
}