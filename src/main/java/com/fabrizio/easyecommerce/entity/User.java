package com.fabrizio.easyecommerce.entity;

import com.fabrizio.easyecommerce.enums.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Null;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name="users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Null
    private Long id;
    @NotBlank
    private String name;
    @Column(unique = true)
    @NotBlank
    private String email;
    @NotBlank
    private String password;
    @Enumerated(EnumType.STRING)
    private Role role;
    @NotBlank
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt =LocalDateTime.now();
     }

}
