package com.exchange.order_completed.infrastructure.dto;

import com.exchange.order_completed.application.OrderType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Builder
public class KafkaOrderStoreEvent {
    private String tradingPair;
    private OrderType orderType;
    private BigDecimal price;
    private BigDecimal quantity;
    private UUID userId;
    private UUID orderId;
    private UUID idempotencyId;
    private long startTime;
}