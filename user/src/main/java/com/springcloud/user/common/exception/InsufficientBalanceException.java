package com.springcloud.user.common.exception;

import java.math.BigDecimal;

public class InsufficientBalanceException extends RuntimeException{
    private final BigDecimal availableBalance; //현재 사용 가능 금액

    public InsufficientBalanceException(String message, BigDecimal availableBalance) {
        super(message);
        this.availableBalance = availableBalance;
    }

    public InsufficientBalanceException(String message, BigDecimal availableBalance, Throwable cause) {
        super(message, cause);
        this.availableBalance = availableBalance;
    }

    public BigDecimal getAvailableBalance() {
        return availableBalance;
    }
}
