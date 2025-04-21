package com.exchange.order.infrastructure.dto;

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
    private String orderType; // buy/sell
    private BigDecimal price; //총 가격
    private BigDecimal amount; // 수량
    private String symbol; //거래소 명칭

}
