package com.exchange.matching.domain.service;

import com.exchange.matching.application.command.CreateTransactionCommand;
import com.exchange.matching.application.query.FindTransactionQuery;
import com.exchange.matching.application.response.TransactionResponse;
import com.exchange.matching.application.response.ListTransactionResponse;
import com.exchange.matching.domain.entiry.TransactionV1;
import com.exchange.matching.infrastructure.repository.TransactionRepositoryV1;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service("cassandra")
@RequiredArgsConstructor
public class TransactionServiceV1 implements TransactionService {

    private final TransactionRepositoryV1 transactionRepositoryV1;

    @Override
    public TransactionResponse saveTransaction(CreateTransactionCommand command) {
        TransactionV1 transactionV1 = new TransactionV1();
        transactionV1.setUserId(command.userId());
        transactionV1.setTransactionId(UUID.randomUUID());
        transactionV1.setTransactionDate(LocalDateTime.now());
        transactionV1.setPrice(command.price());
        transactionV1.setAmount(command.amount());
        transactionV1.setTransactionType(command.transactionType());
        transactionV1.setPair(command.pair());

        TransactionV1 save = transactionRepositoryV1.save(transactionV1);

        return TransactionResponse.from(save);
    }

    @Override
    public ListTransactionResponse findTransactionsByUserId(FindTransactionQuery query, Pageable pageable) {
        Slice<TransactionV1> sliceList = transactionRepositoryV1.findByUserId(query.userId(), pageable);
        return ListTransactionResponse.fromSlice(sliceList);
    }
}