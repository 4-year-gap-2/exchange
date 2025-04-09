package com.exchange.matching.application.command;


import com.exchange.matching.application.dto.enums.OrderType;

import java.math.BigDecimal;
import java.util.UUID;


public record CreateMatchingCommand(
        String tradingPair, // 종목 코드
        OrderType orderType, // 주문 유형 (매수/매도)
        BigDecimal price, // 가격
        BigDecimal quantity, // 수량
        UUID userId // 사용자 ID
) {
}
