package com.fabrizio.easyecommerce.mapper;

import com.fabrizio.easyecommerce.dto.CartRequestDTO;
import com.fabrizio.easyecommerce.dto.CartResponseDTO;
import com.fabrizio.easyecommerce.entity.Cart;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = CartItemMapper.class)
public interface CartMapper {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.email", target = "username")
    // ESTA LÍNEA ES LA QUE QUITA EL WARNING:
    @Mapping(source = "createdAt", target = "createdAt", dateFormat = "yyyy-MM-dd'T'HH:mm:ss")
    @Mapping(target = "total", expression = "java(calculateTotal(cart))")
    CartResponseDTO toCartResponseDTO(Cart cart);

    List<CartResponseDTO> toCartResponseDTOList(List<Cart> carts);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    Cart toCart(CartRequestDTO cartRequestDTO);

    List<Cart> toCartList(List<CartRequestDTO> cartRequestDTOList);

    default java.math.BigDecimal calculateTotal(com.fabrizio.easyecommerce.entity.Cart cart) {
        if (cart.getItems() == null) return java.math.BigDecimal.ZERO;

        return cart.getItems().stream()
                .map(item -> item.getPriceAtPurchase().multiply(java.math.BigDecimal.valueOf(item.getQuantity())))
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
    }
}
