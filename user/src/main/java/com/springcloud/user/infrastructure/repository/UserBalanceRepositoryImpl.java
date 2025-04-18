package com.springcloud.user.infrastructure.repository;

import com.springcloud.user.domain.entity.UserBalance;
import com.springcloud.user.domain.repository.UserBalanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class UserBalanceRepositoryImpl implements UserBalanceRepository {
    private final UserBalanceJpaRepository userBalanceJpaRepository;

    @Override
    public UserBalance save(UserBalance userBalance) {
        return userBalanceJpaRepository.save(userBalance);
    }

    @Override
    public boolean existsByUser_UserIdAndCoin_CoinId(UUID userId, UUID coinId) {
        return userBalanceJpaRepository.existsByUser_UserIdAndCoin_CoinId(userId,coinId);
    }

    @Override
    public Optional<UserBalance> findByWalletWithLock(String wallet) {
        return userBalanceJpaRepository.findByWalletWithLock(wallet);
    }

    @Override
    public Optional<UserBalance> findUserBalanceWithUserAndCoin(UUID userId, String coinId) {
        return userBalanceJpaRepository.findUserBalanceWithUserAndCoin(userId,coinId);
    }
}
