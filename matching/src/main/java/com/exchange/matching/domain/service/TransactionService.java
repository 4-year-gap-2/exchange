package com.exchange.matching.domain.service;

import com.exchange.matching.application.dto.CreateTransactionCommand;
import com.exchange.matching.application.dto.FindTransactionQuery;
import com.exchange.matching.domain.entiry.TransactionV1;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface TransactionService {
    TransactionV1 saveTransaction(CreateTransactionCommand command);
    Slice<TransactionV1> findTransactionsByUserId(FindTransactionQuery query, Pageable pageable);
}
