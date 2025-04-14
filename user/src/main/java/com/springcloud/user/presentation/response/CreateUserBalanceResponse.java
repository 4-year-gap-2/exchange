package com.springcloud.user.presentation.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@RequiredArgsConstructor
@AllArgsConstructor
@Getter
public class CreateUserBalanceResponse {
    private UUID balancedId;
    private UUID userId;
    private UUID coinId;
    private BigDecimal totalBalance;
    private BigDecimal availableBalance;
    private String wallet;
}
