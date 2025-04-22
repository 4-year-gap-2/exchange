package com.exchange.order_completed.infrastructure.dto;

import com.exchange.order_completed.application.command.CreateOrderStoreCommand;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Builder
public class KafkaBalanceIncreaseEvent {

    private String tradingPair;
    private String orderType;
    private BigDecimal price;
    private BigDecimal quantity;
    private UUID userId;
    private UUID orderId;

    public static KafkaBalanceIncreaseEvent from(CreateOrderStoreCommand createOrderStoreCommand) {
        return KafkaBalanceIncreaseEvent.builder()
                .tradingPair(createOrderStoreCommand.tradingPair())
                .orderType(createOrderStoreCommand.orderType())
                .price(createOrderStoreCommand.price())
                .quantity(createOrderStoreCommand.quantity())
                .userId(createOrderStoreCommand.userId())
                .orderId(createOrderStoreCommand.orderId())
                .build();
    }
}
