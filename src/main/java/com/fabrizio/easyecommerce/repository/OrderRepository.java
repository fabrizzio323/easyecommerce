package com.fabrizio.easyecommerce.repository;

import com.fabrizio.easyecommerce.entity.Order;
import com.fabrizio.easyecommerce.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserOrderByCreatedAtDesc(User user);

    List<Order> findByUserEmail(String email);}
