package com.springcloud.user.domain.repository;

import com.springcloud.user.domain.entity.Coin;

import java.util.Optional;
import java.util.UUID;

public interface CoinRepository {
    Optional<Coin> findById(UUID coinId);
}
