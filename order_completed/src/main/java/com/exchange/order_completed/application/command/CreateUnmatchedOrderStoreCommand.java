package com.exchange.order_completed.application.command;

import com.exchange.order_completed.domain.cassandra.entity.OrderState;
import com.exchange.order_completed.domain.cassandra.entity.OrderType;
import com.exchange.order_completed.domain.cassandra.entity.UnmatchedOrder;
import com.exchange.order_completed.infrastructure.dto.KafkaUnmatchedOrderStoreEvent;
import com.exchange.order_completed.infrastructure.enums.OperationType;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
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
        int shard,
        OperationType operationType
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
                event.getShard(),
                event.getOperationType()
        );
    }

    public UnmatchedOrder toEntity() {
        return UnmatchedOrder.builder()
                .tradingPair(tradingPair)
                .orderType(orderType)
                .price(price)
                .quantity(quantity)
                .userId(userId)
                .shard(shard)
                .orderId(orderId)
                .orderState(OrderState.valueOf("PENDING"))
                .createdAt(LocalDateTime.now().atZone(ZoneId.of("UTC")).toInstant())
                .yearMonthDate(yearMonthDate)
                .build();
    }
}