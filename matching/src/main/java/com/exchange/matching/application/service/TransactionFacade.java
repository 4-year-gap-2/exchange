package com.exchange.matching.application.service;

import com.exchange.matching.application.dto.CreateTransactionCommand;
import com.exchange.matching.application.dto.FindTransactionQuery;
import com.exchange.matching.application.dto.ListTransactionResponse;
import com.exchange.matching.domain.entiry.TransactionV1;
import com.exchange.matching.domain.service.TransactionService;
import com.exchange.matching.presentation.dto.CreateTransactionRequest;
import com.exchange.matching.presentation.dto.FindTransactionRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

@Service
public class TransactionFacade {

    private final TransactionService transactionService;

    public TransactionFacade(TransactionService transactionServiceV1) {
        this.transactionService = transactionServiceV1;
    }

    public TransactionV1 createTransaction(CreateTransactionRequest request) {
        CreateTransactionCommand command = CreateTransactionCommand.from(request);
        return transactionService.saveTransaction(command);
    }

    public ListTransactionResponse getTransactionsByUserId(FindTransactionRequest request, Pageable pageable) {
        FindTransactionQuery query = FindTransactionQuery.from(request);
        Slice<TransactionV1> slice = transactionService.findTransactionsByUserId(query, pageable);
        return ListTransactionResponse.from(slice);
    }
}
