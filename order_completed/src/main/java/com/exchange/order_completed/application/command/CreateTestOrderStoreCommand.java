package com.exchange.order_completed.application.command;

import com.exchange.order_completed.domain.cassandra.entity.MatchedOrder;
import com.exchange.order_completed.infrastructure.dto.KafkaMatchedOrderEvent;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

public record CreateTestOrderStoreCommand(
    // 거래 정보
    String tradingPair,
    BigDecimal executionPrice,
    BigDecimal matchedQuantity,

    // 매수 주문 정보
    UUID buyUserId,
    UUID buyOrderId,
    Long buyTimestamp,

    // 매도 주문 정보
    UUID sellUserId,
    UUID sellOrderId,
    Long sellTimestamp,

    UUID matchId,

    Instant createdAt,
    LocalDate yearMonthDate
) {
    public static CreateTestOrderStoreCommand from(KafkaMatchedOrderEvent event) {
        return new CreateTestOrderStoreCommand(
                event.getTradingPair(),
                event.getExecutionPrice(),
                event.getMatchedQuantity(),
                event.getBuyUserId(),
                event.getBuyOrderId(),
                event.getBuyTimestamp(),
                event.getSellUserId(),
                event.getSellOrderId(),
                event.getSellTimestamp(),
                event.getMatchId(),
                LocalDateTime.now().atZone(ZoneId.of("UTC")).toInstant(),
                LocalDate.now(ZoneId.of("UTC"))
        );
    }

    public MatchedOrder toSellOrderEntity() {
        return MatchedOrder.builder()
                .tradingPair(tradingPair)
                .price(executionPrice)
                .quantity(matchedQuantity)
                .userId(sellUserId)
                .orderId(sellOrderId)
                .createdAt(createdAt)
                .yearMonthDate(yearMonthDate)
                .idempotencyId(UUID.randomUUID())
                .orderType("SELL")
                .build();
    }

    public MatchedOrder toBuyOrderEntity() {
        return MatchedOrder.builder()
                .tradingPair(tradingPair)
                .price(executionPrice)
                .quantity(matchedQuantity)
                .userId(buyUserId)
                .orderId(buyOrderId)
                .createdAt(createdAt)
                .yearMonthDate(yearMonthDate)
                .idempotencyId(UUID.randomUUID())
                .orderType("BUY")
                .build();
    }
}