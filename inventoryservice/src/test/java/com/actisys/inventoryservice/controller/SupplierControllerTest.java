package com.actisys.inventoryservice.controller;

import com.actisys.inventoryservice.dto.AddSupplierDTO;
import com.actisys.inventoryservice.dto.SupplierDTO;
import com.actisys.inventoryservice.service.SupplierService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SupplierController.class)
class SupplierControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SupplierService supplierService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private SupplierDTO supplierDTO;

    @BeforeEach
    void setUp() {
        supplierDTO = SupplierDTO.builder().id(1L).name("ACME").phone("12345").address("Main St").build();
    }

    @Test
    @DisplayName("GET /api/v1/suppliers/getAll возвращает список поставщиков")
    void getAll_returnsSuppliers() throws Exception {
        given(supplierService.getAll()).willReturn(List.of(supplierDTO));

        mockMvc.perform(get("/api/v1/suppliers/getAll"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name", is("ACME")));
    }

    @Test
    @DisplayName("POST /api/v1/suppliers/addNew создаёт поставщика")
    void addNew_createsSupplier() throws Exception {
        AddSupplierDTO addDto = AddSupplierDTO.builder().name("ACME").phone("12345").address("Main St").build();

        given(supplierService.addNewSupplier(any(AddSupplierDTO.class))).willReturn(supplierDTO);

        mockMvc.perform(post("/api/v1/suppliers/addNew")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("ACME")));
    }

    @Test
    @DisplayName("PUT /api/v1/suppliers/editSupplier/{id} обновляет поставщика")
    void editSupplier_updatesSupplier() throws Exception {
        AddSupplierDTO updateDto = AddSupplierDTO.builder().name("New").phone("999").address("New Addr").build();

        given(supplierService.editSupplier(anyLong(), any(AddSupplierDTO.class))).willReturn(supplierDTO);

        mockMvc.perform(put("/api/v1/suppliers/editSupplier/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /api/v1/suppliers/deleteSupplier/{id} удаляет поставщика")
    void deleteSupplier_returnsOk() throws Exception {
        willDoNothing().given(supplierService).deleteSupplier(anyLong());

        mockMvc.perform(delete("/api/v1/suppliers/deleteSupplier/{id}", 1L))
                .andExpect(status().isOk());
    }
}
