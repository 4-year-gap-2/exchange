package com.springcloud.management.application.dto;

public enum OrderType {
    BUY,  // 매수
    SELL;// 매도
    public OrderType getOPP(OrderType orderType) {
        if (orderType == BUY) {
            return OrderType.SELL;
        } else {
           return OrderType.BUY;
        }
    }
}