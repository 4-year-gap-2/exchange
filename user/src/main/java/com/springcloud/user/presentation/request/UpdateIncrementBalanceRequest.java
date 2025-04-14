package com.springcloud.user.presentation.request;

import com.springcloud.user.domain.entity.Coin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public class UpdateIncrementBalanceRequest {
    @NotNull
    UUID userId;
    @Positive
    BigDecimal amount;
}
