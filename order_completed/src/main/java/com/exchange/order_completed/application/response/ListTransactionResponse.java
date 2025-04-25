package com.exchange.order_completed.application.response;

import com.exchange.order_completed.domain.entity.TransactionV1;
import com.exchange.order_completed.domain.entity.TransactionV2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Slice;

import java.util.List;

public record ListTransactionResponse(
        List<TransactionResponse> transactions,
        boolean hasNext,
        int pageNumber,
        int pageSize
) {
    public static ListTransactionResponse fromSlice(Slice<TransactionV1> slice) {
        List<TransactionResponse> responses = slice.getContent().stream()
                .map(TransactionResponse::from)
                .toList();

        return new ListTransactionResponse(
                responses,
                slice.hasNext(),
                slice.getNumber(),
                slice.getSize()
        );
    }

    public static ListTransactionResponse fromPage(Page<TransactionV2> page) {
        List<TransactionResponse> responses = page.getContent().stream()
                .map(TransactionResponse::from)
                .toList();

        return new ListTransactionResponse(
                responses,
                page.hasNext(),
                page.getNumber(),
                page.getSize()
        );
    }
}