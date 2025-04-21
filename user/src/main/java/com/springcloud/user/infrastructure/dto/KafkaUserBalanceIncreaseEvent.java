package com.springcloud.user.infrastructure.dto;

import com.springcloud.user.application.enums.OrderType;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class KafkaUserBalanceIncreaseEvent{

    private UUID orderId;
    private String tradingPair;
    private BigDecimal price;
    private BigDecimal quantity;
    private UUID buyer;
    private UUID seller;
    private OrderType orderType;
}
