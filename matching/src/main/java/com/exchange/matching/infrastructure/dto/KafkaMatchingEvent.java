package com.exchange.matching.infrastructure.dto;

import com.exchange.matching.application.command.CreateMatchingCommand;
import com.exchange.matching.application.enums.OrderType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KafkaMatchingEvent {

    private String tradingPair;
    private OrderType orderType;
    private BigDecimal price;
    private BigDecimal quantity;
    private UUID userId;
    private UUID orderId;
    private long startTimeStamp;

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
                command.orderId(),
                System.currentTimeMillis()
        );
    }
}