package com.exchange.order_completed.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class TradeDataRequest {
    private String tradingPair;
    private String orderType;
    private BigDecimal price;
    private BigDecimal quantity;
}
