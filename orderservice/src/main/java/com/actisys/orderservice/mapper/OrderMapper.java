package com.actisys.orderservice.mapper;

import com.actisys.orderservice.dto.OrderDTO;
import com.actisys.orderservice.model.Order;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = OrderInfoMapper.class)
public interface OrderMapper {
    OrderDTO toDTO(Order order);
}
