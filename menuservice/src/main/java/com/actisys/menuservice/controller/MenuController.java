package com.actisys.menuservice.controller;

import com.actisys.menuservice.dto.AddMenuItemDTO;
import com.actisys.menuservice.dto.MenuItemDTO;
import com.actisys.menuservice.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/menu")
public class MenuController {

    private final MenuService menuService;

    @GetMapping("/getAll")
    public ResponseEntity<List<MenuItemDTO>> getAll() {
        return ResponseEntity.ok().body(menuService.getAllMenu());
    }

    @PostMapping("/addNewMenuItem")
    public ResponseEntity<Long> addNewMenuItem(@RequestBody AddMenuItemDTO newMenuItemDTO,
                                               @RequestHeader("X-Username") String userName) {
        return ResponseEntity.ok().body(menuService.addNewMenuItem(userName, newMenuItemDTO));
    }

    @GetMapping("/getMenuItemsByIds")
    public ResponseEntity<List<MenuItemDTO>> getMenuItemsByIds(@RequestParam List<Long> menuIds) {
        return ResponseEntity.ok().body(menuService.getMenuItemsByIds(menuIds));
    }

    @PostMapping("/addNewMenuItemNoRecipe")
    public ResponseEntity<MenuItemDTO> addNewMenuItemNoRecipe(@RequestBody AddMenuItemDTO newMenuItemDTO,
                                                              @RequestHeader("X-Username") String userName) {
        return ResponseEntity.ok().body(menuService.addNewMenuItemNoRecipe(userName, newMenuItemDTO));
    }

    @PutMapping("/editMenuItem/{id}")
    public ResponseEntity<MenuItemDTO> editMenuItem(@PathVariable Long id, @RequestBody MenuItemDTO menuItemDTO) {
        return ResponseEntity.ok().body(menuService.editMenuItem(id, menuItemDTO));
    }

    @PutMapping("/editAvailability/{id}")
    public ResponseEntity<MenuItemDTO> editAvailability(@PathVariable Long id) {
        return ResponseEntity.ok().body(menuService.editAvailability(id));
    }

    @DeleteMapping("/deleteMenuItem/{id}")
    public ResponseEntity<Void> deleteMenuItem(@PathVariable Long id) {
        menuService.deleteMenuItem(id);
        return ResponseEntity.ok().build();
    }
}
