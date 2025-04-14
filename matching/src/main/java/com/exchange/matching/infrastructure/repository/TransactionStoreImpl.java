package com.exchange.matching.infrastructure.repository;

import com.exchange.matching.domain.entiry.Transaction;
import com.exchange.matching.domain.repository.TransactionStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class TransactionStoreImpl implements TransactionStore {

    private final TransactionStoreRepository repository;

    @Override
    public void save(Transaction transaction) {
        repository.save(transaction);
    }

    @Override
    public void deleteAll() {
        repository.deleteAll();
    }

    @Override
    public void saveAll(List<Transaction> transactionList) {
        repository.saveAll(transactionList);
    }
}
