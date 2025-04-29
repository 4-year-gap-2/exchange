package com.exchange.order_completed.infrastructure.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KafkaMatchedOrderStoreEvent {

    private String tradingPair;
    private String orderType;
    private BigDecimal price;
    private BigDecimal quantity;
    private UUID userId;
    private UUID orderId;
    private UUID idempotencyId;
}