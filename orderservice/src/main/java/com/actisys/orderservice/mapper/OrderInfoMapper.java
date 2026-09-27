package com.actisys.orderservice.mapper;

import com.actisys.orderservice.dto.OrderInfoDTO;
import com.actisys.orderservice.model.OrderInfo;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrderInfoMapper {
    OrderInfoDTO toDTO(OrderInfo orderInfo);
}
