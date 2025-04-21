package com.springcloud.management.infrastructure.external.dto;

import com.springcloud.management.application.dto.OrderType;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record NotificationKafkaEvent(UUID orderId,
                                     String tradingPair,
                                     BigDecimal price,
                                     BigDecimal quantity,
                                     UUID buyer,
                                     UUID seller,
                                     OrderType orderType) {
}
