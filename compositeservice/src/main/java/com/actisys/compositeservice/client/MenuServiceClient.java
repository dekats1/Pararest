package com.actisys.compositeservice.client;

import com.actisys.compositeservice.dto.menu.AddMenuItem;
import com.actisys.compositeservice.dto.menu.AddMenuItemDTO;
import com.actisys.compositeservice.dto.order.MenuItemDTO;
import reactor.core.publisher.Mono;

import java.util.List;

public interface MenuServiceClient {
    Mono<Long> createDish(AddMenuItemDTO dishDto, String username);
    Mono<Void> deleteDish(Long dishId, String username);

    Mono<List<MenuItemDTO>> getDishesByIds(List<Long> ids);
}
