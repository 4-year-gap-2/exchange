package com.exchange.matching.application.service;

import com.exchange.matching.application.command.CreateTransactionCommand;
import com.exchange.matching.application.query.FindTransactionQuery;
import com.exchange.matching.application.response.TransactionResponse;
import com.exchange.matching.application.response.ListTransactionResponse;
import com.exchange.matching.domain.service.TransactionService;
import com.exchange.matching.presentation.dto.CreateTransactionRequest;
import com.exchange.matching.presentation.dto.FindTransactionRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class TransactionFacade {

    private final Map<String, TransactionService> transactionServiceMap;

    public TransactionFacade(Map<String, TransactionService> transactionServiceMap) {
        this.transactionServiceMap = transactionServiceMap;
    }

    public TransactionService getService(String type) {
        return transactionServiceMap.getOrDefault(type.toLowerCase(), transactionServiceMap.get("cassandra"));
    }

    public TransactionResponse createTransaction(CreateTransactionRequest request) {
        TransactionService transactionService = getService(request.dataBaseType());
        CreateTransactionCommand command = CreateTransactionCommand.from(request);
        return transactionService.saveTransaction(command);
    }

    public ListTransactionResponse getTransactionsByUserId(FindTransactionRequest request, Pageable pageable) {
        TransactionService transactionService = getService(request.dataBaseType());
        FindTransactionQuery query = FindTransactionQuery.from(request);
        return transactionService.findTransactionsByUserId(query, pageable);
    }
}
