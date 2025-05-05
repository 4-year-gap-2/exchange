package com.exchange.order_completed.application.command;

import com.exchange.order_completed.domain.entity.MatchedOrder;
import com.exchange.order_completed.domain.postgresEntity.Chart;
import com.exchange.order_completed.infrastructure.dto.KafkaMatchedOrderStoreEvent;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

@Builder
public record CreateMatchedOrderStoreCommand(
        String tradingPair,
        String orderType,
        BigDecimal price,
        BigDecimal quantity,
        UUID userId,
        UUID orderId,
        UUID idempotencyId
) {
    public static CreateMatchedOrderStoreCommand from(KafkaMatchedOrderStoreEvent event) {
        return new CreateMatchedOrderStoreCommand(
                event.getTradingPair(),
                event.getOrderType(),
                event.getPrice(),
                event.getQuantity(),
                event.getUserId(),
                event.getOrderId(),
                event.getIdempotencyId()
        );
    }

    public MatchedOrder toEntity(LocalDate yearMonthDate) {
        return MatchedOrder.builder()
                .tradingPair(this.tradingPair)
                .orderType(this.orderType)
                .price(this.price)
                .quantity(this.quantity)
                .userId(this.userId)
                .orderId(this.orderId)
                .idempotencyId(this.idempotencyId)
                .createdAt(LocalDateTime.now().atZone(ZoneId.of("UTC")).toInstant())
                .yearMonthDate(yearMonthDate)
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