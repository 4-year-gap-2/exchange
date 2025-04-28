package com.exchange.order_completed.application.command;

import com.exchange.order_completed.domain.entity.MatchedOrder;
import com.exchange.order_completed.domain.entity.UnmatchedOrder;
import com.exchange.order_completed.domain.postgresEntity.Chart;
import com.exchange.order_completed.infrastructure.dto.KafkaOrderStoreEvent;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
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

    public MatchedOrder toMatchedOrderEntity() {
        return MatchedOrder.builder()
                .tradingPair(tradingPair)
                .orderType(orderType)
                .price(price)
                .quantity(quantity)
                .userId(userId)
                .orderId(orderId)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public UnmatchedOrder toUnmatchedOrderEntity() {
        return UnmatchedOrder.builder()
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
                this.tradingPair,
                null // createdAt 값은 엔티티 내부에서 자동 생성됨
        );
    }
}