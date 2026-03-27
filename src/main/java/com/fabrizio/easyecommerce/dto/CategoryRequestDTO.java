package com.fabrizio.easyecommerce.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryRequestDTO {
   @NotBlank(message = "Id is required")
   private Long id;
   @NotBlank(message = "description is required")
   private String description;
   @NotBlank(message = "id product is required")
   private Long idProduct;
}
