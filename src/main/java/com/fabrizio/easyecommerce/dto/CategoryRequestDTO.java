package com.fabrizio.easyecommerce.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryRequestDTO {
   @NotBlank(message = "name is required")
   private String name;
   @NotBlank(message = "description is required")
   private String description;

}
