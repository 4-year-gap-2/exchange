package com.exchange.order_completed.application.command;

import com.exchange.order_completed.domain.cassandra.entity.OrderState;
import com.exchange.order_completed.domain.cassandra.entity.UnmatchedOrder;
import com.exchange.order_completed.domain.mongodb.entity.MongoUnmatchedOrder;
import com.exchange.order_completed.infrastructure.dto.KafkaUnmatchedOrderStoreEvent;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

@Builder
public record CreateUnmatchedOrderStoreCommand(
        String tradingPair,
        String orderType,
        BigDecimal price,
        BigDecimal quantity,
        UUID userId,
        UUID orderId
) {
    public static CreateUnmatchedOrderStoreCommand from(KafkaUnmatchedOrderStoreEvent event) {
        return new CreateUnmatchedOrderStoreCommand(
                event.getTradingPair(),
                event.getOrderType(),
                event.getPrice(),
                event.getQuantity(),
                event.getUserId(),
                event.getOrderId()
        );
    }

    public UnmatchedOrder toEntity(int shard, LocalDate yearMonthDate) {
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

    public MongoUnmatchedOrder toMongoEntity() {
        return MongoUnmatchedOrder.builder()
                .tradingPair(tradingPair)
                .orderType(orderType)
                .price(price)
                .quantity(quantity)
                .userId(userId)
                .orderId(orderId)
                .createdAt(LocalDateTime.now().atZone(ZoneId.of("UTC")).toInstant())
                .createdDate(LocalDate.now(ZoneId.of("UTC")))
                .build();
    }
}