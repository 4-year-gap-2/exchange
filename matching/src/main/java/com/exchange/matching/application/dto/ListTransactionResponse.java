package com.exchange.matching.application.dto;

import com.exchange.matching.domain.entiry.TransactionV1;
import org.springframework.data.domain.Slice;

import java.util.List;

public record ListTransactionResponse(
        List<TransactionV1> transactions,
        boolean hasNext,
        int pageNumber,
        int pageSize
) {
    public static ListTransactionResponse from(Slice<TransactionV1> slice) {
        return new ListTransactionResponse(
                slice.getContent(),
                slice.hasNext(),
                slice.getNumber(),
                slice.getSize()
        );
    }
}