package com.exchange.web_socket.infrastructure.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MessageEvent {
    private UUID orderId;
    private UUID userId; //유저 아이디
    private BigDecimal price; //총 가격
    private BigDecimal availableBalance; // 현재 잔액
}
