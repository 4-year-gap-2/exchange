package com.exchange.matching.domain.service;

import com.exchange.matching.application.dto.CreateTransactionCommand;
import com.exchange.matching.application.dto.FindTransactionQuery;
import com.exchange.matching.domain.entiry.TransactionV1;
import com.exchange.matching.infrastructure.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionServiceV1 implements TransactionService {

    private final TransactionRepository transactionRepository;

    @Override
    public TransactionV1 saveTransaction(CreateTransactionCommand command) {
        TransactionV1 transactionV1 = new TransactionV1();
        transactionV1.setUserId(command.userId());
        transactionV1.setTransactionId(UUID.randomUUID());
        transactionV1.setTransactionDate(LocalDateTime.now());
        transactionV1.setPrice(command.price());
        transactionV1.setAmount(command.amount());
        transactionV1.setTransactionType(command.transactionType());
        transactionV1.setPair(command.pair());

        return transactionRepository.save(transactionV1);
    }

    @Override
    public Slice<TransactionV1> findTransactionsByUserId(FindTransactionQuery query, Pageable pageable) {
        return transactionRepository.findByUserId(query.userId(), pageable);
    }
}