package com.fabrizio.easyecommerce.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartRequestDTO {
   @NotNull(message = "List items can not be null")
   @NotEmpty(message = "List items can not be empty")
   private List<CartItemRequestDTO> items;
}
