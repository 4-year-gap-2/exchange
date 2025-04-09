package com.exchange.order_completed.domain.service;

import com.exchange.order_completed.application.command.CreateTransactionCommand;
import com.exchange.order_completed.application.response.TransactionResponse;
import com.exchange.order_completed.application.query.FindTransactionQuery;
import com.exchange.order_completed.application.response.ListTransactionResponse;
import org.springframework.data.domain.Pageable;

public interface TransactionService {
    TransactionResponse saveTransaction(CreateTransactionCommand command);
    ListTransactionResponse findTransactionsByUserId(FindTransactionQuery query, Pageable pageable);
}
