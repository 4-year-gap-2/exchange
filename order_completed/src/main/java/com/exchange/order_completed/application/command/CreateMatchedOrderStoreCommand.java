package com.exchange.order_completed.application.command;

import com.exchange.order_completed.domain.cassandra.entity.MatchedOrder;
import com.exchange.order_completed.domain.cassandra.entity.OrderType;
import com.exchange.order_completed.domain.postgres.entity.Chart;
import com.exchange.order_completed.infrastructure.dto.KafkaMatchedOrderStoreEvent;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Builder
public record CreateMatchedOrderStoreCommand(
        String tradingPair,
        BigDecimal price,
        BigDecimal quantity,
        UUID userId,
        UUID matchedOrderId,
        OrderType orderType,
        Instant createdAt,
        LocalDate yearMonthDate,
        int buyShard,
        int sellShard
) {

    public static CreateMatchedOrderStoreCommand fromBuyOrderInfo(KafkaMatchedOrderStoreEvent event) {
        return CreateMatchedOrderStoreCommand.builder()
                .tradingPair(event.getTradingPair())
                .price(event.getExecutionPrice())
                .quantity(event.getMatchedQuantity())
                .userId(event.getBuyUserId())
                .matchedOrderId(event.getBuyMatchedOrderId())
                .orderType(OrderType.BUY)
                .createdAt(event.getCreatedAt())
                .yearMonthDate(event.getYearMonthDate())
                .buyShard(event.getBuyShard())
                .build();
    }

    public static CreateMatchedOrderStoreCommand fromSellOrderInfo(KafkaMatchedOrderStoreEvent event) {
        return CreateMatchedOrderStoreCommand.builder()
                .tradingPair(event.getTradingPair())
                .price(event.getExecutionPrice())
                .quantity(event.getMatchedQuantity())
                .userId(event.getSellUserId())
                .matchedOrderId(event.getSellMatchedOrderId())
                .orderType(OrderType.SELL)
                .createdAt(event.getCreatedAt())
                .yearMonthDate(event.getYearMonthDate())
                .sellShard(event.getSellShard())
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
                .shard(this.orderType == OrderType.BUY ? this.buyShard : this.sellShard)
                .orderType(this.orderType)
                .build();
    }
}