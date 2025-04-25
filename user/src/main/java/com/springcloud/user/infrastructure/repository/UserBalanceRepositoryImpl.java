package com.springcloud.user.infrastructure.repository;

import com.springcloud.user.domain.entity.User;
import com.springcloud.user.domain.entity.UserBalance;
import com.springcloud.user.domain.repository.UserBalanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    public Optional<UserBalance> findByUserAndCoinSymbolForUpdate(User user, String targetCoin) {
        return userBalanceJpaRepository.findByUserAndCoinSymbolForUpdate(user, targetCoin);
    }

    @Override
    public Optional<UserBalance> findUserBalanceWithUserAndCoin(UUID userId, String symbol) {
        return userBalanceJpaRepository.findUserBalanceWithUserAndCoin(userId, symbol);
    }

    @Override
    public Page<UserBalance> findByUser(User user, Pageable pageable) {
        return userBalanceJpaRepository.findByUser(user, pageable);
    }
}
