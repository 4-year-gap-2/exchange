package com.springcloud.user.infrastructure.dto;

import com.springcloud.user.application.enums.OrderType;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
public class MatchCompensatorEvent {
    private UUID orderId;
    private String tradingPair;
    private BigDecimal price;
    private BigDecimal quantity;
    private UUID userId;
    private OrderType orderType;
}
