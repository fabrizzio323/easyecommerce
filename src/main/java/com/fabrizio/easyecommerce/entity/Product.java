package com.fabrizio.easyecommerce.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name="products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotNull
    private String name;
    @NotNull
    private String description;
    @NotNull
    private BigDecimal price;
    @NotNull
    @Min(1)
    private int stock;
    @NotNull
    private boolean active;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="category_id")
    @NotNull
    private Category category;
    @NotNull
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist(){
        this.createdAt=LocalDateTime.now();
    }
}
