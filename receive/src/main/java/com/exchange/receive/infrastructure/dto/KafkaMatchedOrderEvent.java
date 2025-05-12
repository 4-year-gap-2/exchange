package com.exchange.receive.infrastructure.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * 매칭된 주문 정보를 담는 DTO 클래스
 * 매수와 매도 주문 정보를 하나의 객체로 통합
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KafkaMatchedOrderEvent {
    private String tradingPair;
    private BigDecimal executionPrice;
    private BigDecimal matchedQuantity;
    private UUID buyUserId;
    private UUID sellUserId;
    private UUID buyMatchedOrderId;
    private UUID sellMatchedOrderId;
    private Instant createdAt;
    private LocalDate yearMonthDate;
    private int buyShard;
    private int sellShard;
}
