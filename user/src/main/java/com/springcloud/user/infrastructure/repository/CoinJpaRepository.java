package com.springcloud.user.infrastructure.repository;

import com.springcloud.user.domain.entity.Coin;
import com.springcloud.user.domain.repository.CoinRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CoinJpaRepository extends JpaRepository<Coin,UUID>, CoinRepository {

}
