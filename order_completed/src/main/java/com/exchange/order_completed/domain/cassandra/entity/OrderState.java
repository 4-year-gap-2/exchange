package com.exchange.order_completed.domain.cassandra.entity;

public enum OrderState {
    PENDING,    // 미체결
    CANCEL      // 주문취소
}
