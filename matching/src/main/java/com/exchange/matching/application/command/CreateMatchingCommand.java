package com.exchange.matching.application.command;

import com.exchange.matching.application.enums.OrderType;
import com.exchange.matching.presentation.dto.CreateMatchingRequest;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateMatchingCommand(
        String tradingPair, // 종목 코드
        OrderType orderType, // 주문 유형 (매수/매도)
        BigDecimal price, // 가격
        BigDecimal quantity, // 수량
        UUID userId, // 사용자 ID
        UUID orderId
) {
    public static CreateMatchingCommand fromRequest(CreateMatchingRequest createMatchingRequest) {
        return new CreateMatchingCommand(
                createMatchingRequest.tradingPair(),
                createMatchingRequest.orderType(),
                createMatchingRequest.price(),
                createMatchingRequest.quantity(),
                createMatchingRequest.userId(),
                createMatchingRequest.orderId()
        );
    }
}
