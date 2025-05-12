package com.exchange.matching.domain.service;

import com.exchange.matching.application.enums.OrderType;

import java.math.BigDecimal;
import java.util.UUID;

import com.exchange.matching.domain.service.MatchingServiceV1A.Order;

public class OrderFactory {

    public static Order createBuyOrder1() {
        return Order.builder()
                .userId(UUID.randomUUID())
                .orderType(OrderType.BUY)
                .tradingPair("BTC/KRW")
                .price(BigDecimal.valueOf(9000))
                .quantity(BigDecimal.valueOf(0.1))
                .build();
    }

    public static Order createBuyOrder2() {
        return Order.builder()
                .userId(UUID.randomUUID())
                .orderType(OrderType.BUY)
                .tradingPair("BTC/KRW")
                .price(BigDecimal.valueOf(9000))
                .quantity(BigDecimal.valueOf(0.3))
                .build();
    }

    public static Order createBuyOrder3() {
        return Order.builder()
                .userId(UUID.randomUUID())
                .orderType(OrderType.BUY)
                .tradingPair("BTC/KRW")
                .price(BigDecimal.valueOf(8700))
                .quantity(BigDecimal.valueOf(0.1))
                .build();
    }

    public static Order createBuyOrder4() {
        return Order.builder()
                .userId(UUID.randomUUID())
                .orderType(OrderType.BUY)
                .tradingPair("BTC/KRW")
                .price(BigDecimal.valueOf(8900))
                .quantity(BigDecimal.valueOf(0.3))
                .build();
    }

    public static Order createSellOrder1() {
        return Order.builder()
                .userId(UUID.randomUUID())
                .orderType(OrderType.SELL)
                .tradingPair("BTC/KRW")
                .price(BigDecimal.valueOf(9500))
                .quantity(BigDecimal.valueOf(0.3))
                .build();
    }

    public static Order createSellOrder2() {
        return Order.builder()
                .userId(UUID.randomUUID())
                .orderType(OrderType.SELL)
                .tradingPair("BTC/KRW")
                .price(BigDecimal.valueOf(9700))
                .quantity(BigDecimal.valueOf(0.6))
                .build();
    }

    public static Order createSellOrder3() {
        return Order.builder()
                .userId(UUID.randomUUID())
                .orderType(OrderType.SELL)
                .tradingPair("BTC/KRW")
                .price(BigDecimal.valueOf(9700))
                .quantity(BigDecimal.valueOf(0.1))
                .build();
    }

    public static Order createSellOrder4() {
        return Order.builder()
                .userId(UUID.randomUUID())
                .orderType(OrderType.SELL)
                .tradingPair("BTC/KRW")
                .price(BigDecimal.valueOf(10000))
                .quantity(BigDecimal.valueOf(0.2))
                .build();
    }

    public static Order createSellOrder5() {
        return Order.builder()
                .userId(UUID.randomUUID())
                .orderType(OrderType.SELL)
                .tradingPair("BTC/KRW")
                .price(BigDecimal.valueOf(11000))
                .quantity(BigDecimal.valueOf(0.1))
                .build();
    }
}
