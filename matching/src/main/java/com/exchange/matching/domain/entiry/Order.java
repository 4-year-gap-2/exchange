package com.exchange.matching.domain.entiry;

import com.exchange.matching.application.command.CreateMatchingCommand;
import com.exchange.matching.application.dto.enums.OrderType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Builder
public class Order {

    private String tradingPair;         // ("BTC/KRW")
    private OrderType orderType;        // (BUY, SELL)
    private BigDecimal price;
    private BigDecimal quantity;
    private UUID userId;
    private UUID orderId;

    public static Order from(CreateMatchingCommand createMatchingCommand) {
        return Order.builder()
                .tradingPair(createMatchingCommand.tradingPair())
                .orderType(createMatchingCommand.orderType())
                .price(createMatchingCommand.price())
                .quantity(createMatchingCommand.quantity())
                .userId(createMatchingCommand.userId())
                .orderId(createMatchingCommand.orderId())
                .build();
    }
}