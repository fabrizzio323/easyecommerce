package com.fabrizio.easyecommerce.mapper;

import com.fabrizio.easyecommerce.dto.OrderItemResponseDTO;
import com.fabrizio.easyecommerce.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.math.BigDecimal;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface OrderItemMapper {

    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.name", target = "productName")
    @Mapping(source = "price", target = "priceAtPurchase")
    @Mapping(target = "subtotal", expression = "java(calculateSubtotal(item))")
    OrderItemResponseDTO toOrderItemResponseDTO(OrderItem item);

    default BigDecimal calculateSubtotal(OrderItem item) {
        if (item == null || item.getPrice() == null) {
            return BigDecimal.ZERO;
        }
        return item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
    }
}