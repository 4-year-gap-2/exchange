package com.exchange.order_completed.application.command;

import com.exchange.order_completed.domain.cassandra.entity.MatchedOrder;
import com.exchange.order_completed.domain.postgres.entity.Chart;
import com.exchange.order_completed.infrastructure.dto.KafkaMatchedOrderStoreEvent;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Builder
public record CreateMatchedOrderStoreCommand(
        // 거래 정보
        String tradingPair,
        BigDecimal price,
        BigDecimal quantity,

        // 주문 정보
        UUID userId,
        UUID matchedOrderId,
        String orderType,

        // 기타 정보
        Instant createdAt,
        LocalDate yearMonthDate,
        Byte shard
) {

    public static CreateMatchedOrderStoreCommand fromBuyOrderInfo(KafkaMatchedOrderStoreEvent event) {
        return CreateMatchedOrderStoreCommand.builder()
                .tradingPair(event.getTradingPair())
                .price(event.getExecutionPrice())
                .quantity(event.getMatchedQuantity())
                .userId(event.getBuyUserId())
                .matchedOrderId(event.getBuyMatchedOrderId())
                .orderType("BUY")
                .createdAt(event.getCreatedAt())
                .yearMonthDate(event.getYearMonthDate())
                .shard(event.getBuyShard())
                .build();
    }

    public static CreateMatchedOrderStoreCommand fromSellOrderInfo(KafkaMatchedOrderStoreEvent event) {
        return CreateMatchedOrderStoreCommand.builder()
                .tradingPair(event.getTradingPair())
                .price(event.getExecutionPrice())
                .quantity(event.getMatchedQuantity())
                .userId(event.getSellUserId())
                .matchedOrderId(event.getSellMatchedOrderId())
                .orderType("SELL")
                .createdAt(event.getCreatedAt())
                .yearMonthDate(event.getYearMonthDate())
                .shard(event.getSellShard())
                .build();
    }

    public MatchedOrder toEntity() {
        return MatchedOrder.builder()
                .tradingPair(this.tradingPair)
                .price(this.price)
                .quantity(this.quantity)
                .userId(this.userId)
                .matchedOrderId(this.matchedOrderId)
                .createdAt(this.createdAt)
                .yearMonthDate(this.yearMonthDate)
                .shard(this.shard)
                .orderType(this.orderType)
                .build();
    }

    public Chart toChartData(){
        return new Chart(
                UUID.randomUUID(),
                this.price,
                this.quantity,
                this.orderType,
                this.tradingPair,
                null // createdAt 값은 엔티티 내부에서 자동 생성됨
        );
    }
}