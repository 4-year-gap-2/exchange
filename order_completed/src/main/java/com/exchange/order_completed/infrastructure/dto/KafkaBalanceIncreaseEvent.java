package com.exchange.order_completed.infrastructure.dto;

import com.exchange.order_completed.application.command.CreateMatchedOrderStoreCommand;
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
    private UUID matchedOrderId;

    public static KafkaBalanceIncreaseEvent from(CreateMatchedOrderStoreCommand createMatchedOrderStoreCommand) {
        return KafkaBalanceIncreaseEvent.builder()
                .tradingPair(createMatchedOrderStoreCommand.tradingPair())
                .orderType(createMatchedOrderStoreCommand.orderType())
                .price(createMatchedOrderStoreCommand.price())
                .quantity(createMatchedOrderStoreCommand.quantity())
                .userId(createMatchedOrderStoreCommand.userId())
                .matchedOrderId(createMatchedOrderStoreCommand.matchedOrderId())
                .build();
    }
}
