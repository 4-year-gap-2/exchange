package com.exchange.matching.domain.service;

import com.exchange.matching.application.dto.enums.OrderType;
import com.exchange.matching.domain.entiry.ActivatedOrderB;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class ActivatedOrderBFactory {

    private static final String TRADING_PAIR = "BTC/KRW";

    public static ActivatedOrderB createBuyOrder1() {
        return ActivatedOrderB.builder()
                .userId(UUID.randomUUID())
                .orderId(UUID.randomUUID())
                .type(OrderType.BUY)
                .tradingPair(TRADING_PAIR)
                .price(BigDecimal.valueOf(9000))
                .quantity(BigDecimal.valueOf(0.1))
                .createdAt(LocalDateTime.now().minusMinutes(10))
                .build();
    }

    public static ActivatedOrderB createBuyOrder2() {
        return ActivatedOrderB.builder()
                .userId(UUID.randomUUID())
                .orderId(UUID.randomUUID())
                .type(OrderType.BUY)
                .tradingPair(TRADING_PAIR)
                .price(BigDecimal.valueOf(9000))
                .quantity(BigDecimal.valueOf(0.3))
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static ActivatedOrderB createBuyOrder3() {
        return ActivatedOrderB.builder()
                .userId(UUID.randomUUID())
                .orderId(UUID.randomUUID())
                .type(OrderType.BUY)
                .tradingPair(TRADING_PAIR)
                .price(BigDecimal.valueOf(8700))
                .quantity(BigDecimal.valueOf(0.1))
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static ActivatedOrderB createBuyOrder4() {
        return ActivatedOrderB.builder()
                .userId(UUID.randomUUID())
                .orderId(UUID.randomUUID())
                .type(OrderType.BUY)
                .tradingPair(TRADING_PAIR)
                .price(BigDecimal.valueOf(8900))
                .quantity(BigDecimal.valueOf(0.3))
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static ActivatedOrderB createSellOrder1() {
        return ActivatedOrderB.builder()
                .userId(UUID.randomUUID())
                .orderId(UUID.randomUUID())
                .type(OrderType.SELL)
                .tradingPair(TRADING_PAIR)
                .price(BigDecimal.valueOf(9500))
                .quantity(BigDecimal.valueOf(0.3))
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static ActivatedOrderB createSellOrder2() {
        return ActivatedOrderB.builder()
                .userId(UUID.randomUUID())
                .orderId(UUID.randomUUID())
                .type(OrderType.SELL)
                .tradingPair(TRADING_PAIR)
                .price(BigDecimal.valueOf(9700))
                .quantity(BigDecimal.valueOf(0.6))
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static ActivatedOrderB createSellOrder3() {
        return ActivatedOrderB.builder()
                .userId(UUID.randomUUID())
                .orderId(UUID.randomUUID())
                .type(OrderType.SELL)
                .tradingPair(TRADING_PAIR)
                .price(BigDecimal.valueOf(9700))
                .quantity(BigDecimal.valueOf(0.1))
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static ActivatedOrderB createSellOrder4() {
        return ActivatedOrderB.builder()
                .userId(UUID.randomUUID())
                .orderId(UUID.randomUUID())
                .type(OrderType.SELL)
                .tradingPair(TRADING_PAIR)
                .price(BigDecimal.valueOf(10000))
                .quantity(BigDecimal.valueOf(0.2))
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static ActivatedOrderB createSellOrder5() {
        return ActivatedOrderB.builder()
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
