package com.exchange.order_completed.application.service;

import com.exchange.order_completed.application.TimeInterval;
import com.exchange.order_completed.domain.postgres.entity.TradeDataInfo;

import java.util.List;

public interface TradeService {
    List<TradeDataInfo> getTradeInfo(String pair, TimeInterval time);
}
