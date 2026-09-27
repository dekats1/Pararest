package com.actisys.inventoryservice.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddSupplierDTO {
    private String name;
    private String phone;
    private String address;
}
