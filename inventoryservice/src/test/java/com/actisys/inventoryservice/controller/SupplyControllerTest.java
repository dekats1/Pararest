package com.actisys.inventoryservice.controller;

import com.actisys.inventoryservice.dto.AddSupplyDTO;
import com.actisys.inventoryservice.dto.AddSupplyItemDTO;
import com.actisys.inventoryservice.dto.SupplyDTO;
import com.actisys.inventoryservice.model.SupplyStatus;
import com.actisys.inventoryservice.service.SupplyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
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

@WebMvcTest(SupplyController.class)
class SupplyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SupplyService supplyService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("GET /api/v1/supplies/getAll возвращает список поставок")
    void getAll_returnsSupplies() throws Exception {
        SupplyDTO supplyDTO = SupplyDTO.builder().id(1L).status(SupplyStatus.IN_SORT_CENTER).build();
        given(supplyService.getAll()).willReturn(List.of(supplyDTO));

        mockMvc.perform(get("/api/v1/supplies/getAll"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(1)));
    }

    @Test
    @DisplayName("POST /api/v1/supplies/addNew создаёт поставку")
    void addNew_createsSupply() throws Exception {
        AddSupplyItemDTO itemDTO = AddSupplyItemDTO.builder()
                .ingredientId(Long.valueOf(1L))
                .quantity(BigDecimal.valueOf(2))
                .priceAtSupply(BigDecimal.TEN)
                .build();
        AddSupplyDTO addSupplyDTO = AddSupplyDTO.builder().supplierId(Long.valueOf(1L)).items(List.of(itemDTO)).build();
        SupplyDTO responseDto = SupplyDTO.builder().id(Long.valueOf(1L)).status(SupplyStatus.IN_SORT_CENTER).build();

        given(supplyService.addNewSupply(any(AddSupplyDTO.class))).willReturn(responseDto);

        mockMvc.perform(post("/api/v1/supplies/addNew")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addSupplyDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)));
    }

    @Test
    @DisplayName("PUT /api/v1/supplies/updateSupplyStatus/{id} обновляет статус поставки")
    void editSupply_updatesStatus() throws Exception {
        SupplyDTO responseDto = SupplyDTO.builder().id(Long.valueOf(1L)).status(SupplyStatus.SHIPPED).build();

        given(supplyService.editSupply(Long.valueOf(anyLong()), any(SupplyStatus.class))).willReturn(responseDto);

        mockMvc.perform(put("/api/v1/supplies/updateSupplyStatus/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(SupplyStatus.SHIPPED)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("SHIPPED")));
    }

    @Test
    @DisplayName("DELETE /api/v1/supplies/deleteSupply/{id} удаляет поставку")
    void deleteSupply_returnsOk() throws Exception {
        willDoNothing().given(supplyService).deleteSupply(Long.valueOf(anyLong()));

        mockMvc.perform(delete("/api/v1/supplies/deleteSupply/{id}", 1L))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/v1/supplies/totalAmount возвращает общую сумму")
    void getTotalAmount_returnsAmount() throws Exception {
        given(supplyService.getTotalAmount()).willReturn(BigDecimal.valueOf(150));

        mockMvc.perform(get("/api/v1/supplies/totalAmount"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is(150)));
    }
}
