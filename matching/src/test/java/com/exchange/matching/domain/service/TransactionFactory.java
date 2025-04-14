package com.exchange.matching.domain.service;

import com.exchange.matching.application.dto.enums.OrderStatus;
import com.exchange.matching.application.dto.enums.OrderType;
import com.exchange.matching.domain.entiry.Transaction;

import java.math.BigDecimal;
import java.util.UUID;

public class TransactionFactory {

    public static Transaction createBuyOrder1() {
        return Transaction.builder()
                .userId(UUID.randomUUID())
                .type(OrderType.BUY)
                .tradingPair("BTC/KRW")
                .price(BigDecimal.valueOf(9000))
                .quantity(BigDecimal.valueOf(0.1))
                .status(OrderStatus.PENDING)
                .build();
    }

    public static Transaction createBuyOrder2() {
        return Transaction.builder()
                .userId(UUID.randomUUID())
                .type(OrderType.BUY)
                .tradingPair("BTC/KRW")
                .price(BigDecimal.valueOf(9000))
                .quantity(BigDecimal.valueOf(0.3))
                .status(OrderStatus.PENDING)
                .build();
    }

    public static Transaction createBuyOrder3() {
        return Transaction.builder()
                .userId(UUID.randomUUID())
                .type(OrderType.BUY)
                .tradingPair("BTC/KRW")
                .price(BigDecimal.valueOf(8700))
                .quantity(BigDecimal.valueOf(0.1))
                .status(OrderStatus.PENDING)
                .build();
    }

    public static Transaction createBuyOrder4() {
        return Transaction.builder()
                .userId(UUID.randomUUID())
                .type(OrderType.BUY)
                .tradingPair("BTC/KRW")
                .price(BigDecimal.valueOf(8900))
                .quantity(BigDecimal.valueOf(0.3))
                .status(OrderStatus.PENDING)
                .build();
    }

    public static Transaction createSellOrder1() {
        return Transaction.builder()
                .userId(UUID.randomUUID())
                .type(OrderType.SELL)
                .tradingPair("BTC/KRW")
                .price(BigDecimal.valueOf(9500))
                .quantity(BigDecimal.valueOf(0.3))
                .status(OrderStatus.PENDING)
                .build();
    }

    public static Transaction createSellOrder2() {
        return Transaction.builder()
                .userId(UUID.randomUUID())
                .type(OrderType.SELL)
                .tradingPair("BTC/KRW")
                .price(BigDecimal.valueOf(9700))
                .quantity(BigDecimal.valueOf(0.6))
                .status(OrderStatus.PENDING)
                .build();
    }

    public static Transaction createSellOrder3() {
        return Transaction.builder()
                .userId(UUID.randomUUID())
                .type(OrderType.SELL)
                .tradingPair("BTC/KRW")
                .price(BigDecimal.valueOf(9700))
                .quantity(BigDecimal.valueOf(0.1))
                .status(OrderStatus.PENDING)
                .build();
    }

    public static Transaction createSellOrder4() {
        return Transaction.builder()
                .userId(UUID.randomUUID())
                .type(OrderType.SELL)
                .tradingPair("BTC/KRW")
                .price(BigDecimal.valueOf(10000))
                .quantity(BigDecimal.valueOf(0.2))
                .status(OrderStatus.PENDING)
                .build();
    }

    public static Transaction createSellOrder5() {
        return Transaction.builder()
                .userId(UUID.randomUUID())
                .type(OrderType.SELL)
                .tradingPair("BTC/KRW")
                .price(BigDecimal.valueOf(11000))
                .quantity(BigDecimal.valueOf(0.1))
                .status(OrderStatus.PENDING)
                .build();
    }
}
