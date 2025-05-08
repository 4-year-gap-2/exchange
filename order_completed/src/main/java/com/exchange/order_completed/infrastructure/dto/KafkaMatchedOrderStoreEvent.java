package com.exchange.order_completed.infrastructure.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KafkaMatchedOrderStoreEvent {
    // 거래 정보
    private String tradingPair;
    private BigDecimal executionPrice;
    private BigDecimal matchedQuantity;

    // 매수 주문 정보
    private UUID buyUserId;
    private UUID buyMatchedOrderId;

    // 매도 주문 정보
    private UUID sellUserId;
    private UUID sellMatchedOrderId;

    // 기타 정보
    Instant createdAt;
    LocalDate yearMonthDate;
    Byte buyShard;
    Byte sellShard;
}