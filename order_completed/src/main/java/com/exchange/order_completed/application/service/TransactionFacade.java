package com.exchange.order_completed.application.service;

import com.exchange.order_completed.application.command.CreateTransactionCommand;
import com.exchange.order_completed.application.query.FindTransactionQuery;
import com.exchange.order_completed.application.response.TransactionResponse;
import com.exchange.order_completed.application.response.ListTransactionResponse;
import com.exchange.order_completed.domain.service.TransactionService;
import com.exchange.order_completed.infrastructure.postgesql.repository.ChartRepositoryReader;
import com.exchange.order_completed.presentation.dto.CreateTransactionRequest;
import com.exchange.order_completed.presentation.dto.FindTransactionRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class TransactionFacade {

    private final Map<String, TransactionService> transactionServiceMap;
    private final ChartRepositoryReader chartRepositoryReader;
    public TransactionFacade(Map<String, TransactionService> transactionServiceMap, ChartRepositoryReader chartRepositoryReader) {
        this.transactionServiceMap = transactionServiceMap;
        this.chartRepositoryReader = chartRepositoryReader;
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
