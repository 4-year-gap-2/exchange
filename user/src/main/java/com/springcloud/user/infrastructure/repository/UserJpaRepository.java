package com.springcloud.user.infrastructure.repository;

import com.springcloud.user.domain.entity.User;
import com.springcloud.user.domain.repository.UserRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserJpaRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}
