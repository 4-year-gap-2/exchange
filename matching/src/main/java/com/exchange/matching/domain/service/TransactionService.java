package com.exchange.matching.domain.service;

import com.exchange.matching.application.command.CreateTransactionCommand;
import com.exchange.matching.application.response.TransactionResponse;
import com.exchange.matching.application.query.FindTransactionQuery;
import com.exchange.matching.application.response.ListTransactionResponse;
import org.springframework.data.domain.Pageable;

public interface TransactionService {
    TransactionResponse saveTransaction(CreateTransactionCommand command);
    ListTransactionResponse findTransactionsByUserId(FindTransactionQuery query, Pageable pageable);
}
