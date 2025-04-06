package com.exchange.matching.domain.service;

import com.exchange.matching.application.command.CreateTransactionCommand;
import com.exchange.matching.application.query.FindTransactionQuery;
import com.exchange.matching.application.response.TransactionResponse;
import com.exchange.matching.application.response.ListTransactionResponse;
import com.exchange.matching.domain.entiry.TransactionV2;
import com.exchange.matching.infrastructure.repository.TransactionRepositoryV2;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service("mysql")
@RequiredArgsConstructor
public class TransactionServiceV2 implements TransactionService {

    private final TransactionRepositoryV2 transactionRepositoryV2;

    @Override
    public TransactionResponse saveTransaction(CreateTransactionCommand command) {
        TransactionV2 transactionV2 = new TransactionV2();
        transactionV2.setUserId(command.userId());
        transactionV2.setTransactionId(UUID.randomUUID());
        transactionV2.setTransactionDate(LocalDateTime.now());
        transactionV2.setPrice(command.price());
        transactionV2.setAmount(command.amount());
        transactionV2.setTransactionType(command.transactionType());
        transactionV2.setPair(command.pair());

        TransactionV2 save = transactionRepositoryV2.save(transactionV2);

        return TransactionResponse.from(save);
    }

    @Override
    public ListTransactionResponse findTransactionsByUserId(FindTransactionQuery query, Pageable pageable) {
        Page<TransactionV2> pageList = transactionRepositoryV2.findByUserId(query.userId(), pageable);
        return ListTransactionResponse.fromPage(pageList);
    }
}