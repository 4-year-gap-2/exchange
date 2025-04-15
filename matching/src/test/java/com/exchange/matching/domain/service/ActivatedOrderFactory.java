package com.exchange.matching.domain.service;

import com.exchange.matching.application.dto.enums.OrderType;
import com.exchange.matching.domain.entiry.ActivatedOrder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class ActivatedOrderFactory {

    public static ActivatedOrder createBuyOrder1() {
        return ActivatedOrder.builder()
                .userId(UUID.randomUUID())
                .type(OrderType.BUY)
                .tradingPair("BTC/KRW")
                .price(BigDecimal.valueOf(9000))
                .quantity(BigDecimal.valueOf(0.1))
                .createdAt(LocalDateTime.now().minusMinutes(10))
                .build();
    }

    public static ActivatedOrder createBuyOrder2() {
        return ActivatedOrder.builder()
                .userId(UUID.randomUUID())
                .type(OrderType.BUY)
                .tradingPair("BTC/KRW")
                .price(BigDecimal.valueOf(9000))
                .quantity(BigDecimal.valueOf(0.3))
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static ActivatedOrder createBuyOrder3() {
        return ActivatedOrder.builder()
                .userId(UUID.randomUUID())
                .type(OrderType.BUY)
                .tradingPair("BTC/KRW")
                .price(BigDecimal.valueOf(8700))
                .quantity(BigDecimal.valueOf(0.1))
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static ActivatedOrder createBuyOrder4() {
        return ActivatedOrder.builder()
                .userId(UUID.randomUUID())
                .type(OrderType.BUY)
                .tradingPair("BTC/KRW")
                .price(BigDecimal.valueOf(8900))
                .quantity(BigDecimal.valueOf(0.3))
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static ActivatedOrder createSellOrder1() {
        return ActivatedOrder.builder()
                .userId(UUID.randomUUID())
                .type(OrderType.SELL)
                .tradingPair("BTC/KRW")
                .price(BigDecimal.valueOf(9500))
                .quantity(BigDecimal.valueOf(0.3))
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static ActivatedOrder createSellOrder2() {
        return ActivatedOrder.builder()
                .userId(UUID.randomUUID())
                .type(OrderType.SELL)
                .tradingPair("BTC/KRW")
                .price(BigDecimal.valueOf(9700))
                .quantity(BigDecimal.valueOf(0.6))
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static ActivatedOrder createSellOrder3() {
        return ActivatedOrder.builder()
                .userId(UUID.randomUUID())
                .type(OrderType.SELL)
                .tradingPair("BTC/KRW")
                .price(BigDecimal.valueOf(9700))
                .quantity(BigDecimal.valueOf(0.1))
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static ActivatedOrder createSellOrder4() {
        return ActivatedOrder.builder()
                .userId(UUID.randomUUID())
                .type(OrderType.SELL)
                .tradingPair("BTC/KRW")
                .price(BigDecimal.valueOf(10000))
                .quantity(BigDecimal.valueOf(0.2))
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static ActivatedOrder createSellOrder5() {
        return ActivatedOrder.builder()
                .userId(UUID.randomUUID())
                .type(OrderType.SELL)
                .tradingPair("BTC/KRW")
                .price(BigDecimal.valueOf(11000))
                .quantity(BigDecimal.valueOf(0.1))
                .createdAt(LocalDateTime.now())
                .build();
    }
}
