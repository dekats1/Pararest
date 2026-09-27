package com.actisys.inventoryservice.controller;

import com.actisys.inventoryservice.dto.AddSupplierDTO;
import com.actisys.inventoryservice.dto.SupplierDTO;
import com.actisys.inventoryservice.service.SupplierService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/suppliers")
public class SupplierController {

    private final SupplierService supplierService;

    @GetMapping("/getAll")
    public ResponseEntity<List<SupplierDTO>> getAll() {
        return ResponseEntity.ok().body(supplierService.getAll());
    }

    @PostMapping("/addNew")
    public ResponseEntity<SupplierDTO> addNew(@RequestBody AddSupplierDTO supplierDTO) {
        return ResponseEntity.ok().body(supplierService.addNewSupplier(supplierDTO));
    }

    @PutMapping("/editSupplier/{id}")
    public ResponseEntity<SupplierDTO> editSupplier(@PathVariable Long id, @RequestBody AddSupplierDTO supplierDTO) {
        return ResponseEntity.ok().body(supplierService.editSupplier(id, supplierDTO));
    }

    @DeleteMapping("/deleteSupplier/{id}")
    public ResponseEntity<Void> deleteSupplier(@PathVariable Long id) {
        supplierService.deleteSupplier(id);
        return ResponseEntity.ok().build();
    }

}
