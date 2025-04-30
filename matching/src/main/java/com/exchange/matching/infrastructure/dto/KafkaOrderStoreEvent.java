package com.exchange.matching.infrastructure.dto;

import com.exchange.matching.application.dto.enums.OrderType;
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
}