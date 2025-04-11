package com.springcloud.user.infrastructure.repository;

import com.springcloud.user.domain.entity.User;
import com.springcloud.user.domain.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@AllArgsConstructor
public class UserRepositoryImpl implements UserRepository {
    private final UserJpaRepository userJpaRepository;

    @Override
    public Optional<User> findByUsername(String username) {
        //전달하는 쪽이라서 변수만 전달한다.
        return userJpaRepository.findByUsername(username);
    }

    @Override
    public User save(User user) {
        return userJpaRepository.save(user);
    }
}
