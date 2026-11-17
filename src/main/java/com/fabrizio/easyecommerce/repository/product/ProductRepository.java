package com.fabrizio.easyecommerce.repository.product;

import com.fabrizio.easyecommerce.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    Optional<Product> findByIdAndActive(Long id, boolean active);
    List<Product> findByActive(boolean status);
    Page<Product> findByActive(boolean active, Pageable pageable);
}
