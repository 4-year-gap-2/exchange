package com.springcloud.user.application.query;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Builder
public class CheckAvailableBalanceQuery {
    private UUID userId; //유저 아이디
    private String orderType; // buy/sell
    private BigDecimal price; //총 가격
    private BigDecimal amount; // 수량
    private String symbol; //거래소 명칭
}
