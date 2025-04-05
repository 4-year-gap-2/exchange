package com.exchange.matching.application.dto;


import com.exchange.matching.presentation.dto.CreateTransactionRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record CreateTransactionCommand(String userId,
                                       LocalDateTime transactionDate,
                                       UUID transactionId,
                                       BigDecimal price,
                                       BigDecimal amount,
                                       String transactionType,
                                       String pair) {

    public static CreateTransactionCommand from(CreateTransactionRequest request) {
        return new CreateTransactionCommand(
                request.userId(),
                LocalDateTime.now(),  // 현재 시간으로 설정
                UUID.randomUUID(),    // 새로운 UUID 생성
                request.price(),
                request.amount(),
                request.transactionType(),
                request.pair()
        );
    }
}