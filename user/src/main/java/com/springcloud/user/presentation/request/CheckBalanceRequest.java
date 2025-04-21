package com.springcloud.user.presentation.request;

import com.springcloud.user.application.query.CheckAvailableBalanceQuery;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@AllArgsConstructor
@Getter
public class CheckBalanceRequest {
    private UUID userId; //유저 아이디
    private String orderType; // buy/sell
    private BigDecimal price; //총 가격
    private BigDecimal amount; // 수량
    private String symbol; //거래소 명칭


    public CheckAvailableBalanceQuery toQuery() {
        return CheckAvailableBalanceQuery.builder()
                .userId(userId)
                .orderType(orderType)
                .price(price)
                .amount(amount)
                .symbol(symbol)
                .build();
    }
}
