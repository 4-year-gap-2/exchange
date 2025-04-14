package com.exchange.matching.domain.entiry;

import com.exchange.matching.application.dto.enums.OrderStatus;
import com.exchange.matching.application.dto.enums.OrderType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Builder
public class Order {

    private String tradingPair;         // (BTC/KRW)
    private OrderType orderType;        // (BUY, SELL)
    private BigDecimal price;
    private BigDecimal quantity;
    private OrderStatus orderStatus;    //(PENDING, COMPLETED)
    private UUID userId;

    private Order(String tradingPair, OrderType orderType, BigDecimal price, BigDecimal quantity, OrderStatus orderStatus, UUID userId) {
        this.tradingPair = tradingPair;
        this.orderType = orderType;
        this.price = price;
        this.quantity = quantity;
        this.userId = userId;
        this.orderStatus = orderStatus;
    }

    public static Order from(String tradingPair, OrderType orderType, BigDecimal price, BigDecimal quantity, UUID userId) {
        return new Order(tradingPair, orderType, price, quantity, OrderStatus.PENDING, userId);
    }
}