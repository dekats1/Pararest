package com.actisys.analyticsservice.service.impl;

import com.actisys.analyticsservice.client.MenuServiceClient;
import com.actisys.analyticsservice.client.InventoryServiceClient;
import com.actisys.analyticsservice.client.OrderServiceClient;
import com.actisys.analyticsservice.client.PaymentServiceClient;
import com.actisys.analyticsservice.dto.AnalyticsDTO;
import com.actisys.analyticsservice.dto.MenuItemAnalyticsDTO;
import com.actisys.analyticsservice.dto.OrderAnalyticsDTO;
import com.actisys.analyticsservice.dto.OrderInfoAnalyticsDTO;
import com.actisys.analyticsservice.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements AnalyticsService {

    private final PaymentServiceClient paymentServiceClient;
    private final InventoryServiceClient inventoryServiceClient;
    private final OrderServiceClient orderServiceClient;
    private final MenuServiceClient menuServiceClient;

    @Override
    public Mono<AnalyticsDTO> getMonthlyAnalytics() {
        log.info("Fetching monthly analytics from all services");

        Mono<BigDecimal> revenueMono = paymentServiceClient.getRevenueAmount()
                .onErrorResume(e -> {
                    log.error("Failed to fetch revenue, defaulting to 0: {}", e.getMessage());
                    return Mono.just(BigDecimal.ZERO);
                });

        Mono<Long> revenueOperationsMono = paymentServiceClient.getRevenueOperationsCount()
                .onErrorResume(e -> {
                    log.error("Failed to fetch revenue operations, defaulting to 0: {}", e.getMessage());
                    return Mono.just(0L);
                });

        Mono<BigDecimal> expensesMono = inventoryServiceClient.getTotalSuppliesAmount()
                .onErrorResume(e -> {
                    log.error("Failed to fetch expenses, defaulting to 0: {}", e.getMessage());
                    return Mono.just(BigDecimal.ZERO);
                });

        Mono<Long> orderCountMono = orderServiceClient.getTotalCount()
                .onErrorResume(e -> {
                    log.error("Failed to fetch order count, defaulting to 0: {}", e.getMessage());
                    return Mono.just(0L);
                });

        Mono<List<OrderAnalyticsDTO>> ordersMono = orderServiceClient.getAllOrders()
                .onErrorResume(e -> {
                    log.error("Failed to fetch orders list for top dish, defaulting to empty: {}", e.getMessage());
                    return Mono.just(List.of());
                });

        Mono<List<MenuItemAnalyticsDTO>> menuItemsMono = menuServiceClient.getAllMenuItems()
                .onErrorResume(e -> {
                    log.error("Failed to fetch menu items for top dish, defaulting to empty: {}", e.getMessage());
                    return Mono.just(List.of());
                });

        return Mono.zip(revenueMono, expensesMono, orderCountMono, revenueOperationsMono, ordersMono, menuItemsMono)
                .map(tuple -> {
                    BigDecimal revenue = tuple.getT1();
                    BigDecimal expenses = tuple.getT2();
                    Long orderCount = tuple.getT3();
                    Long revenueOperationsCount = tuple.getT4();
                    List<OrderAnalyticsDTO> orders = tuple.getT5();
                    List<MenuItemAnalyticsDTO> menuItems = tuple.getT6();

                    BigDecimal netProfit = revenue.subtract(expenses);
                    BigDecimal averageCheck = revenueOperationsCount > 0
                            ? revenue.divide(BigDecimal.valueOf(revenueOperationsCount), 2, RoundingMode.HALF_UP)
                            : BigDecimal.ZERO;

                    Map<Long, Long> dishSales = aggregateDishSales(orders);
                    Map<Long, String> menuMap = menuItems.stream()
                            .collect(Collectors.toMap(MenuItemAnalyticsDTO::getId, MenuItemAnalyticsDTO::getName, (a, b) -> a));
                    Map.Entry<Long, Long> topDish = dishSales.entrySet().stream()
                            .max(Map.Entry.comparingByValue(Comparator.naturalOrder()))
                            .orElse(null);
                    String topDishName = topDish == null
                            ? "—"
                            : menuMap.getOrDefault(topDish.getKey(), "Блюдо #" + topDish.getKey());
                    Long topDishCount = topDish == null ? 0L : topDish.getValue();

                    log.info("Analytics assembled: revenue={}, expenses={}, netProfit={}, orders={}, operations={}",
                            revenue, expenses, netProfit, orderCount, revenueOperationsCount);

                    return AnalyticsDTO.builder()
                            .totalRevenue(revenue)
                            .totalExpenses(expenses)
                            .netProfit(netProfit)
                            .totalOrders(orderCount)
                            .averageCheck(averageCheck)
                            .topDishName(topDishName)
                            .topDishCount(topDishCount)
                            .build();
                });
    }

    private Map<Long, Long> aggregateDishSales(List<OrderAnalyticsDTO> orders) {
        return orders.stream()
                .filter(order -> order.getStatus() != null && !"IN_CART".equals(order.getStatus()))
                .filter(order -> order.getStatus() != null && !"DISCARDED".equals(order.getStatus()))
                .filter(order -> order.getStatus() != null && !"CANCELED".equals(order.getStatus()))
                .filter(order -> order.getPaymentStatus() == null || !"REFUNDED".equals(order.getPaymentStatus()))
                .flatMap(order -> order.getItems() == null ? Stream.<OrderInfoAnalyticsDTO>empty() : order.getItems().stream())
                .filter(item -> item.getMenuItemId() != null && item.getQuantity() != null && item.getQuantity() > 0)
                .collect(Collectors.groupingBy(
                        item -> item.getMenuItemId(),
                        Collectors.summingLong(item -> item.getQuantity().longValue())
                ));
    }
}