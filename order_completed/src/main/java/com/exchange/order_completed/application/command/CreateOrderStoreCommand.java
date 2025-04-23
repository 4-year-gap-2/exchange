package com.exchange.order_completed.application.command;

import com.exchange.order_completed.domain.entiry.CompletedOrder;
import com.exchange.order_completed.domain.postgresEntity.Chart;
import com.exchange.order_completed.infrastructure.dto.KafkaOrderStoreEvent;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record CreateOrderStoreCommand(
        String tradingPair,
        String orderType,
        BigDecimal price,
        BigDecimal quantity,
        UUID userId,
        UUID orderId
) {
    public static CreateOrderStoreCommand from(KafkaOrderStoreEvent event) {
        return new CreateOrderStoreCommand(
                event.getTradingPair(),
                event.getOrderType(),
                event.getPrice(),
                event.getQuantity(),
                event.getUserId(),
                event.getOrderId()
        );
    }

    public CompletedOrder toEntity() {
        return CompletedOrder.builder()
                .tradingPair(tradingPair)
                .orderType(orderType)
                .price(price)
                .quantity(quantity)
                .userId(userId)
                .orderId(orderId)
                .createdAt(LocalDateTime.now())
                .build();
    }
    public Chart toChartData(){
        return new Chart(
                UUID.randomUUID(),
                this.price,
                this.quantity,
                this.orderType,
                this.tradingPair
        );
    }
}