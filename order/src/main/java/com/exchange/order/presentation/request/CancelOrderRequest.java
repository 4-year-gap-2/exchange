package com.exchange.order.presentation.request;

import com.exchange.order.application.enums.OrderType;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
public class CancelOrderRequest {
    String tradingPair;
    OrderType orderType; // "BUY" or "SELL"
    long timestamp;
    BigDecimal quantity;
    UUID userId;
    UUID orderId;

}
