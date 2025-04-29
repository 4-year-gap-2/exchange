package com.exchange.order_completed.infrastructure.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateOrderStoreRequest(
        String tradingPair,
        String orderType,
        BigDecimal price,
        BigDecimal quantity,
        UUID userId,
        UUID orderId,
        UUID idempotencyId
) {
}
