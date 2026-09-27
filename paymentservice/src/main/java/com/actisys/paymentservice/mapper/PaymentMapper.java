package com.actisys.paymentservice.mapper;

import com.actisys.paymentservice.dto.PaymentDTO;
import com.actisys.paymentservice.model.Payment;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

@Component
@Mapper(componentModel = "spring")
public interface PaymentMapper {
    PaymentDTO toDTO(Payment payment);
}
