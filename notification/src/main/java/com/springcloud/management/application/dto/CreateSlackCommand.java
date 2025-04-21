package com.springcloud.management.application.dto;

import com.springcloud.management.infrastructure.external.dto.NotificationKafkaEvent;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateSlackCommand(UUID orderId,
                                 String tradingPair,
                                 BigDecimal price,
                                 BigDecimal quantity,
                                 UUID buyer,
                                 UUID seller,
                                 OrderType orderType) {

    public static CreateSlackCommand fromEvent(NotificationKafkaEvent createSlackRequest){
        return new CreateSlackCommand(
                createSlackRequest.orderId(),
                createSlackRequest.tradingPair(),
                createSlackRequest.price(),
                createSlackRequest.quantity(),
                createSlackRequest.buyer(),
                createSlackRequest.seller(),
                createSlackRequest.orderType()
        );
    }

    public String createMessage(String topic){
        return this.toString() + topic;
    }
}
