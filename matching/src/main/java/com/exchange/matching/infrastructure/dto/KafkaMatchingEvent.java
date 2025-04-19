package com.exchange.matching.infrastructure.dto;

import com.exchange.matching.application.command.CreateMatchingCommand;
import com.exchange.matching.application.dto.enums.OrderType;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@NoArgsConstructor
public class KafkaMatchingEvent {

    private String tradingPair;
    private OrderType orderType;
    private BigDecimal price;
    private BigDecimal quantity;
    private UUID userId;
    private UUID orderId;

    public KafkaMatchingEvent(String tradingPair, OrderType orderType, BigDecimal price, BigDecimal quantity, UUID userId, UUID orderId) {
        this.tradingPair = tradingPair;
        this.orderType = orderType;
        this.price = price;
        this.quantity = quantity;
        this.userId = userId;
        this.orderId = orderId;
    }

    public static CreateMatchingCommand commandFromEvent(KafkaMatchingEvent event) {
        return new CreateMatchingCommand(
                event.tradingPair,
                event.orderType,
                event.price,
                event.quantity,
                event.userId,
                event.orderId
        );
    }

    public static KafkaMatchingEvent fromCommand(CreateMatchingCommand command) {
        return new KafkaMatchingEvent(
                command.tradingPair(),
                command.orderType(),
                command.price(),
                command.quantity(),
                command.userId(),
                UUID.randomUUID()
        );
    }
}