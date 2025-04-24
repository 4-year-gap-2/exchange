package com.springcloud.user.application.query;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;
@AllArgsConstructor
@RequiredArgsConstructor
@Getter
public class FindUserBalanceQuery {
    private UUID balancedId;
    private UUID userId;
    private UUID coinId;
    private BigDecimal totalBalance;
    private BigDecimal availableBalance;
    private String wallet;

}
