package com.fabrizio.easyecommerce.repository;

import com.fabrizio.easyecommerce.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findByNameAndStatus(String name, boolean status);
    List<Product> findByStatus(boolean status);
}
