package com.exchange.order_completed.domain.service;

import com.exchange.order_completed.application.command.CreateTransactionCommand;
import com.exchange.order_completed.application.query.FindTransactionQuery;
import com.exchange.order_completed.application.response.TransactionResponse;
import com.exchange.order_completed.application.response.ListTransactionResponse;
import com.exchange.order_completed.domain.entity.TransactionV1;
import com.exchange.order_completed.domain.repository.TransactionReaderV1;
import com.exchange.order_completed.domain.repository.TransactionStoreV1;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service("cassandra")
@RequiredArgsConstructor
public class TransactionServiceV1 implements TransactionService {

    private final TransactionStoreV1 transactionStoreV1;
    private final TransactionReaderV1 transactionReaderV1;

    @Override
    public TransactionResponse saveTransaction(CreateTransactionCommand command) {
        TransactionV1 transactionV1 = new TransactionV1();

        LocalDateTime now = LocalDateTime.now();

        //파티션 스큐를 막기위한 월정보 추가
        String yearMonth = now.format(DateTimeFormatter.ofPattern("yyyy-MM"));

        transactionV1.setUserId(command.userId());
        transactionV1.setYearMonth(yearMonth);
        transactionV1.setTransactionId(UUID.randomUUID());
        transactionV1.setTransactionDate(LocalDateTime.now());
        transactionV1.setPrice(command.price());
        transactionV1.setAmount(command.amount());
        transactionV1.setTransactionType(command.transactionType());
        transactionV1.setPair(command.pair());

        TransactionV1 save = transactionStoreV1.saveWithConsistencyLevel(transactionV1);

        return TransactionResponse.from(save);
    }

    @Override
    public ListTransactionResponse findTransactionsByUserId(FindTransactionQuery query, Pageable pageable) {
        Slice<TransactionV1> sliceList = transactionReaderV1.findByUserIdWithConsistencyLevel(query, pageable);
        return ListTransactionResponse.fromSlice(sliceList);
    }
}