package com.exchange.matching.infrastructure.dto;

import com.exchange.matching.application.command.CreateMatchingCommand;
import com.exchange.matching.application.dto.enums.OrderType;
import com.exchange.matching.presentation.dto.CreateMatchingRequest;

import java.math.BigDecimal;
import java.util.UUID;

public record KafkaMatchingEvent(
        String tradingPair,
        OrderType orderType,
        BigDecimal price,
        BigDecimal quantity,
        UUID userId,
        UUID orderId
) {
    public static CreateMatchingCommand commandFromEvent(KafkaMatchingEvent event){
        return new CreateMatchingCommand(
                event.tradingPair,
                event.orderType,
                event.price,
                event.quantity,
                event.userId);
    }

    public static KafkaMatchingEvent fromRequest(CreateMatchingRequest request) {
        return new KafkaMatchingEvent(
                request.tradingPair(),
                request.orderType(),
                request.price(),
                request.quantity(),
                request.userId(),
                UUID.randomUUID());
    }
}