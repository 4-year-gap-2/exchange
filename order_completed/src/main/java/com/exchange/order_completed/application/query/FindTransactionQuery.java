package com.exchange.order_completed.application.query;

import com.exchange.order_completed.presentation.dto.FindTransactionRequest;

import java.util.UUID;

public record FindTransactionQuery(UUID userId,
                                   String yearMonth,
                                   String dataBaseType) {
    public static FindTransactionQuery from(FindTransactionRequest request) {
        return new FindTransactionQuery(
                request.userId(),
                request.yearMonth(),
                request.dataBaseType()
        );
    }
}
