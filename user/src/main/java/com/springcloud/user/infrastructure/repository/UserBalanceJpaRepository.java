package com.springcloud.user.infrastructure.repository;

import com.springcloud.user.domain.entity.UserBalance;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;


@Repository
public interface UserBalanceJpaRepository extends JpaRepository<UserBalance, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000"))
    @Query("SELECT b FROM UserBalance b WHERE b.wallet = :wallet")
    Optional<UserBalance> findByWalletWithLock(@Param("wallet") String wallet);

    boolean existsByUser_UserIdAndCoin_CoinId(UUID userId, UUID coinId);

    @Query("SELECT ub FROM UserBalance ub " +
            "JOIN FETCH ub.user u " +
            "JOIN FETCH ub.coin c " +
            "WHERE u.userId = :userId AND c.coinName = :coinId")
    Optional<UserBalance> findUserBalanceWithUserAndCoin(@Param("userId") UUID userId,
                                                         @Param("coinId") String coinId);
}
