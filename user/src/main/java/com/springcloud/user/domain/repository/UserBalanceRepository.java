package com.springcloud.user.domain.repository;

import com.springcloud.user.domain.entity.UserBalance;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserBalanceRepository {
    UserBalance save(UserBalance userBalance);

    boolean existsByUser_UserIdAndCoin_CoinId(UUID userId, UUID coinId);

    Optional<UserBalance> findByWalletWithLock(@Param("wallet") String wallet);



    Optional<UserBalance> findUserBalanceWithUserAndCoin(@Param("userId") UUID userId,
                                                         @Param("coinId") String coinId);
}
