package com.exchange.matching.presentation.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateTransactionRequest(UUID userId,
                                       BigDecimal price,
                                       BigDecimal amount,
                                       String transactionType,
                                       String pair,
                                       String dataBaseType) {
}
