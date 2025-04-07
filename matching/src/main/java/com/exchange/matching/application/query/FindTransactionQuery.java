package com.exchange.matching.application.query;

import com.exchange.matching.presentation.dto.FindTransactionRequest;

import java.util.UUID;

public record FindTransactionQuery(UUID userId) {
    public static FindTransactionQuery from(FindTransactionRequest request) {
        return new FindTransactionQuery(
                request.userId()
        );
    }
}
