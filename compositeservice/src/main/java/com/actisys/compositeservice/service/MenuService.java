package com.actisys.compositeservice.service;

import com.actisys.compositeservice.dto.menu.AddMenuItem;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;

public interface MenuService {
    Mono<ResponseEntity<Boolean>> addNewDish(AddMenuItem menuItem, String username);
}
