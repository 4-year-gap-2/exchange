package com.springcloud.user.domain.repository;

import com.springcloud.user.domain.entity.User;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository {
    Optional<User> findByUsername(String username);

    User save(User user);

    Optional<User> findById(UUID userId);
}
