package com.fabrizio.easyecommerce.mapper;

import com.fabrizio.easyecommerce.dto.CartItemRequestDTO;
import com.fabrizio.easyecommerce.dto.CartItemResponseDTO;
import com.fabrizio.easyecommerce.entity.CartItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CartItemMapper {

    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.name", target = "productName")
    @Mapping(target = "subtotal", expression = "java(cartItem.getPriceAtPurchase().multiply(java.math.BigDecimal.valueOf(cartItem.getQuantity())))")
    CartItemResponseDTO toCartItemResponseDTO(CartItem cartItem);

    List<CartItemResponseDTO> toCartItemResponseDTOList(List<CartItem> cartItems);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cart", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "priceAtPurchase", ignore = true)
    CartItem toCartItem(CartItemRequestDTO cartItemRequestDTO);

    List<CartItem> toCartItemList(List<CartItemRequestDTO> cartItemRequestDTOList);
}

