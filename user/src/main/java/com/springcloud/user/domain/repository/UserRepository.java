package com.springcloud.user.domain.repository;

import com.springcloud.user.domain.entity.User;

import java.util.Optional;
import java.util.UUID;


public interface UserRepository {
    Optional<User> findByUsername(String username);

    User save(User user);

    Optional<User> findById(UUID userId);
}
