package com.springcloud.user.presentation.response;

import com.springcloud.user.application.result.FindUserBalanceResult;
import com.springcloud.user.domain.entity.UserBalance;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@RequiredArgsConstructor
@AllArgsConstructor
@Getter
public class FindUserBalanceResponse {
    private UUID balancedId;
    private UUID userId;
    private UUID coinId;
    private BigDecimal totalBalance;
    private BigDecimal availableBalance;
    private String wallet;

    public static FindUserBalanceResponse from(FindUserBalanceResult result) {
        return new FindUserBalanceResponse(
                result.getBalanceId(),
                result.getUserId(),
                result.getCoinId(),
                result.getTotalBalance(),
                result.getAvailableBalance(),
                result.getWallet()
        );
    }
}
