package com.fabrizio.easyecommerce.entity;

import com.fabrizio.easyecommerce.enums.Role;

import java.time.LocalDateTime;

public class User {

    private Long id;
    private String name;
    private String email;
    private String password;
    private Role role;
    private LocalDateTime createdAt;
}
