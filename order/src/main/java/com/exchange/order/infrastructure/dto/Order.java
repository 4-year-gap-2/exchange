package com.exchange.order.infrastructure.dto;

import com.exchange.order.application.enums.OrderType;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order implements Serializable {

    private UUID userId; //유저 아이디
    private OrderType orderType; // buy/sell
    private BigDecimal price; //총 가격
    private BigDecimal quantity; // 수량
    private String tradingPair;

}
