package com.fabrizio.easyecommerce.mapper;

import com.fabrizio.easyecommerce.dto.OrderResponseDTO;
import com.fabrizio.easyecommerce.entity.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {OrderItemMapper.class})
public interface OrderMapper {

    @Mapping(source = "user.email", target = "userEmail")
    OrderResponseDTO toOrderResponseDTO(Order order);
}
