package com.fabrizio.easyecommerce.repository;

import com.fabrizio.easyecommerce.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    Optional<Cart> findByUserEmail(String email);

    Optional<Cart> findByUserId(Long userId);

}
