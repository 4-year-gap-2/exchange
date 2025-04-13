package com.exchange.matching.domain.service;

import com.exchange.matching.application.dto.enums.OrderStatus;
import com.exchange.matching.application.dto.enums.OrderType;
import com.exchange.matching.domain.entiry.TransactionB;

import java.math.BigDecimal;
import java.util.UUID;

public class TransactionBFactory {

    public static TransactionB createBuyOrder1() {
        return TransactionB.builder()
                .userId(UUID.randomUUID())
                .type(OrderType.BUY)
                .tradingPair("BTC/KRW")
                .price(BigDecimal.valueOf(9000))
                .quantity(BigDecimal.valueOf(0.1))
                .status(OrderStatus.PENDING)
                .build();
    }

    public static TransactionB createBuyOrder2() {
        return TransactionB.builder()
                .userId(UUID.randomUUID())
                .type(OrderType.BUY)
                .tradingPair("BTC/KRW")
                .price(BigDecimal.valueOf(9000))
                .quantity(BigDecimal.valueOf(0.3))
                .status(OrderStatus.PENDING)
                .build();
    }

    public static TransactionB createBuyOrder3() {
        return TransactionB.builder()
                .userId(UUID.randomUUID())
                .type(OrderType.BUY)
                .tradingPair("BTC/KRW")
                .price(BigDecimal.valueOf(8700))
                .quantity(BigDecimal.valueOf(0.1))
                .status(OrderStatus.PENDING)
                .build();
    }

    public static TransactionB createBuyOrder4() {
        return TransactionB.builder()
                .userId(UUID.randomUUID())
                .type(OrderType.BUY)
                .tradingPair("BTC/KRW")
                .price(BigDecimal.valueOf(8900))
                .quantity(BigDecimal.valueOf(0.3))
                .status(OrderStatus.PENDING)
                .build();
    }

    public static TransactionB createSellOrder1() {
        return TransactionB.builder()
                .userId(UUID.randomUUID())
                .type(OrderType.SELL)
                .tradingPair("BTC/KRW")
                .price(BigDecimal.valueOf(9500))
                .quantity(BigDecimal.valueOf(0.3))
                .status(OrderStatus.PENDING)
                .build();
    }

    public static TransactionB createSellOrder2() {
        return TransactionB.builder()
                .userId(UUID.randomUUID())
                .type(OrderType.SELL)
                .tradingPair("BTC/KRW")
                .price(BigDecimal.valueOf(9700))
                .quantity(BigDecimal.valueOf(0.6))
                .status(OrderStatus.PENDING)
                .build();
    }

    public static TransactionB createSellOrder3() {
        return TransactionB.builder()
                .userId(UUID.randomUUID())
                .type(OrderType.SELL)
                .tradingPair("BTC/KRW")
                .price(BigDecimal.valueOf(9700))
                .quantity(BigDecimal.valueOf(0.1))
                .status(OrderStatus.PENDING)
                .build();
    }

    public static TransactionB createSellOrder4() {
        return TransactionB.builder()
                .userId(UUID.randomUUID())
                .type(OrderType.SELL)
                .tradingPair("BTC/KRW")
                .price(BigDecimal.valueOf(10000))
                .quantity(BigDecimal.valueOf(0.2))
                .status(OrderStatus.PENDING)
                .build();
    }

    public static TransactionB createSellOrder5() {
        return TransactionB.builder()
                .userId(UUID.randomUUID())
                .type(OrderType.SELL)
                .tradingPair("BTC/KRW")
                .price(BigDecimal.valueOf(11000))
                .quantity(BigDecimal.valueOf(0.1))
                .status(OrderStatus.PENDING)
                .build();
    }
}
