package com.springcloud.user.infrastructure.repository;

import com.springcloud.user.domain.entity.Coin;
import com.springcloud.user.domain.repository.CoinRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CoinJpaRepository extends JpaRepository<Coin,UUID>, CoinRepository {

}
