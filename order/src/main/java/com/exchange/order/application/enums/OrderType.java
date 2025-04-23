package com.exchange.order.application.enums;

public enum OrderType {
    BUY,  // 매수
    SELL;// 매도
    public static OrderType getOPP(OrderType orderType) {
        if (orderType == BUY) {
            return OrderType.SELL;
        } else {
           return OrderType.BUY;
        }
    }
}