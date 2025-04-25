package com.springcloud.user.infrastructure.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class KafkaInsufficientBalanceEvent {
    private UUID orderId;
    private UUID userId; //유저 아이디
    private BigDecimal price; //총 가격
    private BigDecimal availableBalance; // 현재 잔액

    public static KafkaInsufficientBalanceEvent from(UUID orderId, UUID userId, BigDecimal price, BigDecimal availableBalance) {
        return new KafkaInsufficientBalanceEvent(
                orderId,
                userId,
                price,
                availableBalance
        );
    }
}
