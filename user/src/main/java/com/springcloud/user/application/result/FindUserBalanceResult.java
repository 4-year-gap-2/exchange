package com.springcloud.user.application.result;

import com.springcloud.user.domain.entity.UserBalance;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@RequiredArgsConstructor
@AllArgsConstructor
@Getter
public class FindUserBalanceResult {
    private UUID balanceId;
    private UUID userId;
    private UUID coinId;
    private BigDecimal totalBalance;
    private BigDecimal availableBalance;
    private String wallet;

    public static FindUserBalanceResult from(UserBalance balance) {
        return new FindUserBalanceResult(
                balance.getBalanceId(),
                balance.getUser().getUserId(),
                balance.getCoin().getCoinId(),
                balance.getTotalBalance(),
                balance.getAvailableBalance(),
                balance.getWallet()
        );
    }
}
