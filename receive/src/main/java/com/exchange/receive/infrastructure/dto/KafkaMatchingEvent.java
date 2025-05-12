package com.exchange.receive.infrastructure.dto;

import com.exchange.receive.infrastructure.enums.OrderType;
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
public class KafkaMatchingEvent {

    private String tradingPair;
    private OrderType orderType;
    private BigDecimal price;
    private BigDecimal quantity;
    private UUID userId;
    private UUID orderId;
}