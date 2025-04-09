package com.exchange.order_completed.presentation.dto;

import java.util.UUID;

public record FindTransactionRequest(UUID userId,
                                     String yearMonth,
                                     String dataBaseType) {
}