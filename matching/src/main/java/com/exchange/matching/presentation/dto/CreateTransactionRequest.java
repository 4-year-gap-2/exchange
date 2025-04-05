package com.exchange.matching.presentation.dto;

import java.math.BigDecimal;

public record CreateTransactionRequest(String userId,
                                       BigDecimal price,
                                       BigDecimal amount,
                                       String transactionType,
                                       String pair) {
}
