package com.actisys.compositeservice.dto.menu;

import lombok.*;

import java.util.HashMap;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddMenuItem {
    private AddMenuItemDTO item;
    private HashMap<String, Integer> ingredientsMap;
}
