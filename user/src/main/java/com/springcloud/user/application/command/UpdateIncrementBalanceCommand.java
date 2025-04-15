package com.springcloud.user.application.command;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class UpdateIncrementBalanceCommand {
    private String wallet;
    private BigDecimal amount;
}
