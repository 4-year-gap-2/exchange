package com.exchange.receive.infrastructure.dto;

import com.exchange.receive.infrastructure.enums.OperationType;
import com.exchange.receive.infrastructure.enums.OrderType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
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
    private OperationType operationType;
    private int shard;
    private LocalDate yearMonthDate;
    private long startTime;
    private Instant createdAt;
}