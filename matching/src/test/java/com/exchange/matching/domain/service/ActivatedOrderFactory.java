package com.exchange.matching.domain.service;

import com.exchange.matching.application.enums.OrderType;
import com.exchange.matching.domain.entity.UnmatchedOrderA;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class ActivatedOrderFactory {

    private static final String TRADING_PAIR = "BTC/KRW";

    public static UnmatchedOrderA createBuyOrder1() {
        return UnmatchedOrderA.builder()
                .userId(UUID.randomUUID())
                .orderId(UUID.randomUUID())
                .type(OrderType.BUY)
                .tradingPair(TRADING_PAIR)
                .price(BigDecimal.valueOf(9000))
                .quantity(BigDecimal.valueOf(0.1))
                .createdAt(LocalDateTime.now().minusMinutes(10))
                .build();
    }

    public static UnmatchedOrderA createBuyOrder2() {
        return UnmatchedOrderA.builder()
                .userId(UUID.randomUUID())
                .orderId(UUID.randomUUID())
                .type(OrderType.BUY)
                .tradingPair(TRADING_PAIR)
                .price(BigDecimal.valueOf(9000))
                .quantity(BigDecimal.valueOf(0.3))
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static UnmatchedOrderA createBuyOrder3() {
        return UnmatchedOrderA.builder()
                .userId(UUID.randomUUID())
                .orderId(UUID.randomUUID())
                .type(OrderType.BUY)
                .tradingPair(TRADING_PAIR)
                .price(BigDecimal.valueOf(8700))
                .quantity(BigDecimal.valueOf(0.1))
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static UnmatchedOrderA createBuyOrder4() {
        return UnmatchedOrderA.builder()
                .userId(UUID.randomUUID())
                .orderId(UUID.randomUUID())
                .type(OrderType.BUY)
                .tradingPair(TRADING_PAIR)
                .price(BigDecimal.valueOf(8900))
                .quantity(BigDecimal.valueOf(0.3))
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static UnmatchedOrderA createSellOrder1() {
        return UnmatchedOrderA.builder()
                .userId(UUID.randomUUID())
                .orderId(UUID.randomUUID())
                .type(OrderType.SELL)
                .tradingPair(TRADING_PAIR)
                .price(BigDecimal.valueOf(9500))
                .quantity(BigDecimal.valueOf(0.3))
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static UnmatchedOrderA createSellOrder2() {
        return UnmatchedOrderA.builder()
                .userId(UUID.randomUUID())
                .orderId(UUID.randomUUID())
                .type(OrderType.SELL)
                .tradingPair(TRADING_PAIR)
                .price(BigDecimal.valueOf(9700))
                .quantity(BigDecimal.valueOf(0.6))
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static UnmatchedOrderA createSellOrder3() {
        return UnmatchedOrderA.builder()
                .userId(UUID.randomUUID())
                .orderId(UUID.randomUUID())
                .type(OrderType.SELL)
                .tradingPair(TRADING_PAIR)
                .price(BigDecimal.valueOf(9700))
                .quantity(BigDecimal.valueOf(0.1))
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static UnmatchedOrderA createSellOrder4() {
        return UnmatchedOrderA.builder()
                .userId(UUID.randomUUID())
                .orderId(UUID.randomUUID())
                .type(OrderType.SELL)
                .tradingPair(TRADING_PAIR)
                .price(BigDecimal.valueOf(10000))
                .quantity(BigDecimal.valueOf(0.2))
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static UnmatchedOrderA createSellOrder5() {
        return UnmatchedOrderA.builder()
                .userId(UUID.randomUUID())
                .orderId(UUID.randomUUID())
                .type(OrderType.SELL)
                .tradingPair(TRADING_PAIR)
                .price(BigDecimal.valueOf(11000))
                .quantity(BigDecimal.valueOf(0.1))
                .createdAt(LocalDateTime.now())
                .build();
    }
}
