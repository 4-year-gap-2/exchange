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
    // 거래 정보
    private String tradingPair;
    private BigDecimal executionPrice;
    private BigDecimal matchedQuantity;

    // 매수 주문 정보
    private UUID buyUserId;
    @Builder.Default
    private UUID buyMatchedOrderId = UUID.randomUUID();

    // 매도 주문 정보
    private UUID sellUserId;
    @Builder.Default
    private UUID sellMatchedOrderId = UUID.randomUUID();

    // 매칭 ID (생성 시 자동 할당)
    @Builder.Default
    private UUID matchId = UUID.randomUUID();

    // 기타 정보
    Instant createdAt;
    LocalDate yearMonthDate;
    byte buyShard;
    byte sellShard;
}
