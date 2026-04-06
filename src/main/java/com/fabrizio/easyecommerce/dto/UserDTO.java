package com.fabrizio.easyecommerce.dto;

import com.fabrizio.easyecommerce.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {
    private Long id;
    private String name;
    private String street;
    private String city;
    private String zipCode;
    private String email;
    private String password;
    private Role role;
}

