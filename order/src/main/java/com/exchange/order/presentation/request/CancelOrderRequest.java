package com.exchange.order.presentation.request;

import lombok.Getter;

@Getter
public class CancelOrderRequest {
    String tradingPair;
    String orderType; // "BUY" or "SELL"
    String timestamp;
    String quantity;
    String userId;
    String orderId;

}
