package com.actisys.compositeservice.service.impl;

import com.actisys.compositeservice.client.MenuServiceClient;
import com.actisys.compositeservice.client.OrderServiceClient;
import com.actisys.compositeservice.dto.order.FullOrderDTO;
import com.actisys.compositeservice.dto.order.MenuItemDTO;
import com.actisys.compositeservice.dto.order.OrderInfoDTO;
import com.actisys.compositeservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderServiceClient orderServiceClient;
    private final MenuServiceClient menuServiceClient;

    @Override
    public Mono<ResponseEntity<List<FullOrderDTO>>> getFullOrders(String id){
        return orderServiceClient.getOrdersByUserId(id)
                .flatMap(orders -> {
                    if (orders == null || orders.isEmpty()) {
                        return Mono.just(new ResponseEntity<List<FullOrderDTO>>(List.of(), HttpStatus.OK));
                    }

                    List<Long> menuItemIds = orders.stream()
                            .flatMap(order -> order.getItems().stream())
                            .map(OrderInfoDTO::getMenuItemId)
                            .distinct()
                            .toList();

                    return menuServiceClient.getDishesByIds(menuItemIds)
                            .map(menuItems -> {
                                Map<Long, MenuItemDTO> menuItemMap = menuItems.stream()
                                        .collect(Collectors.toMap(MenuItemDTO::getId, item -> item));

                                List<FullOrderDTO> fullOrders = orders.stream()
                                        .map(order -> {
                                            List<MenuItemDTO> orderMenuItems = order.getItems().stream()
                                                    .map(OrderInfoDTO::getMenuItemId)
                                                    .map(menuItemMap::get)
                                                    .filter(Objects::nonNull)
                                                    .toList();

                                            FullOrderDTO fullOrder = new FullOrderDTO();
                                            fullOrder.setOrder(order);
                                            fullOrder.setMenuItem(orderMenuItems);
                                            return fullOrder;
                                        })
                                        .toList();

                                return new ResponseEntity<>(fullOrders, HttpStatus.OK);
                            });
                })
                .onErrorResume(e -> {
                    log.error("Failed to get full orders for user {}: {}", id, e.getMessage());
                    return Mono.just(new ResponseEntity<>(List.of(), HttpStatus.INTERNAL_SERVER_ERROR));
                });
    }


    @Override
    public Mono<ResponseEntity<FullOrderDTO>> getCart(String userId) {
        return orderServiceClient.getCart(userId)
                .flatMap(order -> {
                    if (order == null || order.getItems() == null || order.getItems().isEmpty()) {
                        FullOrderDTO empty = new FullOrderDTO();
                        empty.setOrder(order);
                        empty.setMenuItem(List.of());
                        return Mono.just(new ResponseEntity<>(empty, HttpStatus.OK));
                    }

                    List<Long> menuItemIds = order.getItems().stream()
                            .map(OrderInfoDTO::getMenuItemId)
                            .distinct()
                            .toList();

                    return menuServiceClient.getDishesByIds(menuItemIds)
                            .map(menuItems -> {
                                FullOrderDTO fullOrder = new FullOrderDTO();
                                fullOrder.setOrder(order);
                                fullOrder.setMenuItem(menuItems);
                                return new ResponseEntity<>(fullOrder, HttpStatus.OK);
                            });
                })
                .onErrorResume(e -> {
                    log.error("Failed to get cart for user {}: {}", userId, e.getMessage());
                    return Mono.just(ResponseEntity.internalServerError().build());
                });
    }
}
