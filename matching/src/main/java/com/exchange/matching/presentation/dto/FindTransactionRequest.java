package com.exchange.matching.presentation.dto;

import java.util.UUID;

public record FindTransactionRequest(UUID userId,
                                     String dataBaseType) {
}