package com.exchange.matching.application.response;

import com.exchange.matching.domain.entiry.TransactionV1;
import com.exchange.matching.domain.entiry.TransactionV2;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record TransactionResponse(
        UUID transactionId,
        UUID userId,
        LocalDateTime transactionDate,
        BigDecimal price,
        BigDecimal amount,
        String transactionType,
        String pair
) {

    public static TransactionResponse from(TransactionV1 v1) {
        return new TransactionResponse(
                v1.getTransactionId(),
                v1.getUserId(),
                v1.getTransactionDate(),
                v1.getPrice(),
                v1.getAmount(),
                v1.getTransactionType(),
                v1.getPair()
        );
    }

    public static TransactionResponse from(TransactionV2 v2) {
        return new TransactionResponse(
                v2.getTransactionId(),
                v2.getUserId(),
                v2.getTransactionDate(),
                v2.getPrice(),
                v2.getAmount(),
                v2.getTransactionType(),
                v2.getPair()
        );
    }
}
