package com.springcloud.user.infrastructure.repository;

import com.springcloud.user.domain.entity.User;
import com.springcloud.user.domain.entity.UserBalance;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;


public interface UserBalanceJpaRepository extends JpaRepository<UserBalance, UUID>{

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000"))
    @Query("SELECT b FROM UserBalance b WHERE b.wallet = :wallet")
    Optional<UserBalance> findByWalletWithLock(@Param("wallet") String wallet);

    boolean existsByUser_UserIdAndCoin_CoinId(UUID userId, UUID coinId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM UserBalance b JOIN b.coin c WHERE b.user = :user AND c.symbol = :symbol")
    Optional<UserBalance> findByUserAndCoinSymbolForUpdate(@Param("user") User user, @Param("symbol") String symbol);

    @Query(value = "SELECT ub FROM UserBalance ub " +
            "JOIN FETCH ub.user u " +
            "JOIN FETCH ub.coin c " +
            "WHERE u.userId = :userId AND c.symbol = :symbol")
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<UserBalance> findUserBalanceWithUserAndCoin(@Param("userId") UUID userId,
                                                         @Param("symbol") String symbol);

    Page<UserBalance> findByUser(User user, Pageable pageable);
}
