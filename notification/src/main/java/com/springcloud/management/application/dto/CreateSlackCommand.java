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
        return String.format(
                "에러가 발생했습니다.\n" +
                        "에러 토픽: %s\n" +
                        "발생 이벤트 정보\n" +
                        "  주문 ID: %s\n" +
                        "  거래 쌍: %s\n" +
                        "  가격: %s\n" +
                        "  수량: %s\n" +
                        "  구매자 ID: %s\n" +
                        "  판매자 ID: %s\n" +
                        "  주문 유형: %s",
                topic,
                this.orderId(),
                this.tradingPair(),
                this.price(),
                this.quantity(),
                this.buyer(),
                this.seller(),
                this.orderType()
        );
    }
}
