package com.springcloud.user.presentation.response;

import java.math.BigDecimal;
import java.util.UUID;

public class UpdateIncrementBalanceResponse {
    private UUID balancedId;
    private UUID userId;
    private UUID coinId;
    private BigDecimal totalBalance;
    private BigDecimal availableBalance;
    private String wallet;
}
