package com.springcloud.user.application.command;

import com.springcloud.user.domain.entity.CoinType;
import com.springcloud.user.domain.entity.User;
import com.springcloud.user.domain.entity.UserBalance;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserBalanceCommandService {
    // UserBalance 생성 로직 (User 객체 파라미터 필요)
    public List<UserBalance> createInitialBalances(User user) {
        return Arrays.stream(CoinType.values())
                .map(coinType -> buildUserBalance(user, CoinType.valueOf(coinType.name())))
                .toList();
    }

    private UserBalance buildUserBalance(User user, CoinType coinType) {
        return UserBalance.builder()
                .user(user)  // 외부에서 전달받은 User 사용
                .coinType(coinType)
                .wallet(generateWalletAddress(coinType))
                .totalBalance(BigDecimal.ZERO)
                .availableBalance(BigDecimal.ZERO)
                .build();
    }

    // 지갑 주소 생성
    private String generateWalletAddress(CoinType coinType) {
        return switch (coinType) {
            case SOL -> "SOL_" + UUID.randomUUID().toString().substring(0, 8);
            case ETH -> "ETH_0x" + UUID.randomUUID().toString().substring(0, 8);
            case BTC -> "BTC_1" + UUID.randomUUID().toString().substring(0, 8);
        };
    }
}
