package com.exchange.matching.presentation.dto;

import com.exchange.matching.application.enums.OrderType;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateMatchingRequest(
        String tradingPair,
        OrderType orderType,
        BigDecimal price,
        BigDecimal quantity,
        UUID userId,
        UUID orderId) {
}
