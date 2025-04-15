package com.exchange.matching.application.dto.enums;

import com.exchange.matching.domain.entiry.Order;

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