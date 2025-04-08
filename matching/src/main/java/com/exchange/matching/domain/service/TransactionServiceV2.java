package com.exchange.matching.domain.service;

import com.exchange.matching.application.command.CreateTransactionCommand;
import com.exchange.matching.application.query.FindTransactionQuery;
import com.exchange.matching.application.response.TransactionResponse;
import com.exchange.matching.application.response.ListTransactionResponse;
import com.exchange.matching.domain.entiry.TransactionV2;
import com.exchange.matching.domain.repository.TransactionReaderV1;
import com.exchange.matching.domain.repository.TransactionReaderV2;
import com.exchange.matching.infrastructure.repository.TransactionRepositoryReaderV2;
import com.exchange.matching.infrastructure.repository.TransactionRepositoryStoreV1;
import com.exchange.matching.infrastructure.repository.TransactionRepositoryStoreV2;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service("mysql")
@RequiredArgsConstructor
public class TransactionServiceV2 implements TransactionService {

    private final TransactionRepositoryReaderV2 transactionRepositoryReaderV2;
    private final TransactionRepositoryStoreV2 transactionRepositoryStoreV2;

    @Override
    public TransactionResponse saveTransaction(CreateTransactionCommand command) {

        LocalDateTime now = LocalDateTime.now();

        String yearMonth = now.format(DateTimeFormatter.ofPattern("yyyy-MM"));

        TransactionV2 transactionV2 = new TransactionV2();
        transactionV2.setUserId(command.userId());
        transactionV2.setYearMonth(yearMonth);
        transactionV2.setTransactionId(UUID.randomUUID());
        transactionV2.setTransactionDate(LocalDateTime.now());
        transactionV2.setPrice(command.price());
        transactionV2.setAmount(command.amount());
        transactionV2.setTransactionType(command.transactionType());
        transactionV2.setPair(command.pair());

        TransactionV2 save = transactionRepositoryStoreV2.save(transactionV2);

        return TransactionResponse.from(save);
    }

    @Override
    public ListTransactionResponse findTransactionsByUserId(FindTransactionQuery query, Pageable pageable) {
        Page<TransactionV2> pageList = transactionRepositoryReaderV2.findByUserIdAndYearMonth(query.userId(), query.yearMonth(), pageable);
        return ListTransactionResponse.fromPage(pageList);
    }
}