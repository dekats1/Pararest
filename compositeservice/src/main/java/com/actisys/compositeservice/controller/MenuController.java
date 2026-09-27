package com.actisys.compositeservice.controller;

import com.actisys.compositeservice.dto.menu.AddMenuItem;
import com.actisys.compositeservice.service.MenuService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/composite/menu")
public class MenuController {

    private final MenuService menuService;

    @PostMapping("/addNewDish")
    public Mono<ResponseEntity<Boolean>> addNewDish(
            @RequestBody AddMenuItem menuItem,
            @RequestHeader(value = "X-Username", required = false) String username) {
        return menuService.addNewDish(menuItem, username);
    }

}
