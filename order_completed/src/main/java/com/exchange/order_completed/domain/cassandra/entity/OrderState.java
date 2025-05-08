package com.exchange.order_completed.domain.cassandra.entity;

import com.netflix.spectator.api.Utils;

public enum OrderState {
    PENDING,  // 매수
    CANCEL;// 매도


}
