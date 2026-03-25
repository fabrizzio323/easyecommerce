package com.fabrizio.easyecommerce.repository;


import com.fabrizio.easyecommerce.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.swing.text.html.Option;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByname(String name);
    Optional<User> findByEmail(String email);
}
