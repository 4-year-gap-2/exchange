package com.springcloud.user.domain.repository;

import com.springcloud.user.domain.entity.UserBalance;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserBalanceRepository {
    UserBalance save(UserBalance userBalance);

    boolean existsByUser_UserIdAndCoin_CoinId(UUID userId, UUID coinId);
}
