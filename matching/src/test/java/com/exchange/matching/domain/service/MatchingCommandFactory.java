package com.exchange.matching.domain.service;

import com.exchange.matching.application.command.CreateMatchingCommand;
import com.exchange.matching.application.enums.OrderType;

import java.math.BigDecimal;
import java.util.UUID;

public class MatchingCommandFactory {

    private static final String TRADING_PAIR = "BTC/KRW";

    public static CreateMatchingCommand createBuyOrder1() {
        return new CreateMatchingCommand(
                TRADING_PAIR,
                OrderType.BUY,
                BigDecimal.valueOf(9000),   // Price
                BigDecimal.valueOf(0.1),    // Quantity
                UUID.randomUUID(),  // User ID
                UUID.randomUUID()   // Order ID
        );
    }

    public static CreateMatchingCommand createBuyOrder2() {
        return new CreateMatchingCommand(
                TRADING_PAIR,
                OrderType.BUY,
                BigDecimal.valueOf(9000),   // Price
                BigDecimal.valueOf(0.3),    // Quantity
                UUID.randomUUID(),  // User ID
                UUID.randomUUID()   // Order ID
        );
    }

    public static CreateMatchingCommand createBuyOrder3() {
        return new CreateMatchingCommand(
                TRADING_PAIR,
                OrderType.BUY,
                BigDecimal.valueOf(8700),   // Price
                BigDecimal.valueOf(0.1),    // Quantity
                UUID.randomUUID(),  // User ID
                UUID.randomUUID()   // Order ID
        );
    }

    public static CreateMatchingCommand createBuyOrder4() {
        return new CreateMatchingCommand(
                TRADING_PAIR,
                OrderType.BUY,
                BigDecimal.valueOf(8900),   // Price
                BigDecimal.valueOf(0.3),    // Quantity
                UUID.randomUUID(),  // User ID
                UUID.randomUUID()   // Order ID
        );
    }

    public static CreateMatchingCommand createSellOrder1() {
        return new CreateMatchingCommand(
                TRADING_PAIR,
                OrderType.SELL,
                BigDecimal.valueOf(9500),   // Price
                BigDecimal.valueOf(0.3),    // Quantity
                UUID.randomUUID(),  // User ID
                UUID.randomUUID()   // Order ID
        );
    }

    public static CreateMatchingCommand createSellOrder2() {
        return new CreateMatchingCommand(
                TRADING_PAIR,
                OrderType.SELL,
                BigDecimal.valueOf(9700),   // Price
                BigDecimal.valueOf(0.6),    // Quantity
                UUID.randomUUID(),  // User ID
                UUID.randomUUID()   // Order ID
        );
    }

    public static CreateMatchingCommand createSellOrder3() {
        return new CreateMatchingCommand(
                TRADING_PAIR,
                OrderType.SELL,
                BigDecimal.valueOf(9700),   // Price
                BigDecimal.valueOf(0.1),    // Quantity
                UUID.randomUUID(),  // User ID
                UUID.randomUUID()   // Order ID
        );
    }

    public static CreateMatchingCommand createSellOrder4() {
        return new CreateMatchingCommand(
                TRADING_PAIR,
                OrderType.SELL,
                BigDecimal.valueOf(10000),   // Price
                BigDecimal.valueOf(0.2),    // Quantity
                UUID.randomUUID(),  // User ID
                UUID.randomUUID()   // Order ID
        );
    }

    public static CreateMatchingCommand createSellOrder5() {
        return new CreateMatchingCommand(
                TRADING_PAIR,
                OrderType.SELL,
                BigDecimal.valueOf(11000),   // Price
                BigDecimal.valueOf(0.1),    // Quantity
                UUID.randomUUID(),  // User ID
                UUID.randomUUID()   // Order ID
        );
    }
}
