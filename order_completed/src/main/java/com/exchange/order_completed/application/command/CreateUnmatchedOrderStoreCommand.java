package com.exchange.order_completed.application.command;

import com.exchange.order_completed.domain.cassandra.entity.ColdDataOrders;
import com.exchange.order_completed.domain.cassandra.entity.OrderState;
import com.exchange.order_completed.domain.cassandra.entity.OrderType;
import com.exchange.order_completed.domain.cassandra.entity.UnmatchedOrder;
import com.exchange.order_completed.infrastructure.dto.KafkaUnmatchedOrderStoreEvent;
import com.exchange.order_completed.infrastructure.enums.OperationType;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Builder
public record CreateUnmatchedOrderStoreCommand(
        String tradingPair,
        OrderType orderType,
        BigDecimal price,
        BigDecimal quantity,
        UUID userId,
        UUID orderId,
        LocalDate yearMonthDate,
        long timestamp,
        int shard,
        OperationType operationType,
        Instant createdAt
) {
    public static CreateUnmatchedOrderStoreCommand from(KafkaUnmatchedOrderStoreEvent event) {
        return new CreateUnmatchedOrderStoreCommand(
                event.getTradingPair(),
                event.getOrderType(),
                event.getPrice(),
                event.getQuantity(),
                event.getUserId(),
                event.getOrderId(),
                event.getYearMonthDate(),
                event.getStartTime(),
                event.getShard(),
                event.getOperationType(),
                event.getCreatedAt()
        );
    }

    public UnmatchedOrder unmatchedOrderToEntity() {
        return UnmatchedOrder.builder()
                .tradingPair(tradingPair)
                .orderType(orderType)
                .price(price)
                .quantity(quantity)
                .userId(userId)
                .shard(shard)
                .orderId(orderId)
                .orderState(OrderState.PENDING)
                .createdAt(createdAt)
                .yearMonthDate(yearMonthDate)
                .build();
    }

    public ColdDataOrders coldDataOrdersToEntity() {
        return ColdDataOrders.builder()
                .tradingPair(tradingPair)
                .orderType(orderType)
                .priceOrder(orderType.equals(OrderType.BUY) ? "DESC" : "ASC")
                .price(price)
                .quantity(quantity)
                .userId(userId)
                .orderId(orderId)
                .orderState(OrderState.PENDING)
                .timestamp(timestamp)
                .build();
    }
}