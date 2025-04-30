package com.exchange.order_completed.infrastructure.dto;

import com.exchange.order_completed.application.OrderType;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
public class KafkaMatchingEvent {

    private String tradingPair;
    private OrderType orderType;
    private BigDecimal price;
    private BigDecimal quantity;
    private UUID userId;
    private UUID orderId;

    public KafkaMatchingEvent(String tradingPair, OrderType orderType, BigDecimal price, BigDecimal quantity, UUID userId, UUID orderId) {
        this.tradingPair = tradingPair;
        this.orderType = orderType;
        this.price = price;
        this.quantity = quantity;
        this.userId = userId;
        this.orderId = orderId;
    }
}