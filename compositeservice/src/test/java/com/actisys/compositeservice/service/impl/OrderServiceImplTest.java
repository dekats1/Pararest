package com.actisys.compositeservice.service.impl;

import com.actisys.compositeservice.client.MenuServiceClient;
import com.actisys.compositeservice.client.OrderServiceClient;
import com.actisys.compositeservice.dto.order.MenuItemDTO;
import com.actisys.compositeservice.dto.order.OrderDTO;
import com.actisys.compositeservice.dto.order.OrderInfoDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderServiceClient orderServiceClient;
    @Mock
    private MenuServiceClient menuServiceClient;

    @InjectMocks
    private OrderServiceImpl orderService;

    @Test
    void getFullOrders_mergesOrderItemsWithMenuItems() {
        OrderDTO order = OrderDTO.builder()
                .id(1L)
                .userId(UUID.randomUUID())
                .items(List.of(OrderInfoDTO.builder().menuItemId(100L).quantity(2).build()))
                .build();
        MenuItemDTO menu = MenuItemDTO.builder().id(100L).name("Soup").build();
        when(orderServiceClient.getOrdersByUserId("u-1")).thenReturn(Mono.just(List.of(order)));
        when(menuServiceClient.getDishesByIds(List.of(100L))).thenReturn(Mono.just(List.of(menu)));

        StepVerifier.create(orderService.getFullOrders("u-1"))
                .assertNext(resp -> {
                    assertEquals(HttpStatus.OK, resp.getStatusCode());
                    assertEquals(1, resp.getBody().size());
                    assertEquals("Soup", resp.getBody().get(0).getMenuItem().get(0).getName());
                })
                .verifyComplete();
    }
}
