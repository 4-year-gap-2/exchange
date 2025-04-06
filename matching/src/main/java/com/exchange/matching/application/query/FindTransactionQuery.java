package com.exchange.matching.application.query;

import com.exchange.matching.presentation.dto.FindTransactionRequest;

public record FindTransactionQuery(String userId) {
    public static FindTransactionQuery from(FindTransactionRequest request) {
        return new FindTransactionQuery(
                request.userId()
        );
    }
}
