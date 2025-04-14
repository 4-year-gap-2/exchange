package com.springcloud.user.domain.repository;

import com.springcloud.user.domain.entity.Coin;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CoinRepository {
    Optional<Coin> findById(UUID coinId);
}
