package com.fabrizio.easyecommerce.dto;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartItemRequestDTO {
    @NotNull(message = "Product ID cannot be null")
    private Long productId;
    @Min(value = 1, message = "Quantity must be at least 1")
    private int quantity;
}
