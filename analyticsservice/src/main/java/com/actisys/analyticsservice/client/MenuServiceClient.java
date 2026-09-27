package com.actisys.analyticsservice.client;

import com.actisys.analyticsservice.dto.MenuItemAnalyticsDTO;
import reactor.core.publisher.Mono;

import java.util.List;

public interface MenuServiceClient {
    Mono<List<MenuItemAnalyticsDTO>> getAllMenuItems();
}
