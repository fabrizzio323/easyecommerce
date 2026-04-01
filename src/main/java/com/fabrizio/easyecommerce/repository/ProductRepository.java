package com.fabrizio.easyecommerce.repository;

import com.fabrizio.easyecommerce.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findByIdAndActive(Long id, boolean active);
    List<Product> findByActive(boolean status);
}
