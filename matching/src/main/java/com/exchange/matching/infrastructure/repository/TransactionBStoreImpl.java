package com.exchange.matching.infrastructure.repository;

import com.exchange.matching.domain.entiry.TransactionB;
import com.exchange.matching.domain.repository.TransactionBStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class TransactionBStoreImpl implements TransactionBStore {

    private final TransactionBStoreRepository repository;

    @Override
    public void save(TransactionB transaction) {
        repository.save(transaction);
    }

    @Override
    public void deleteAll() {
        repository.deleteAll();
    }

    @Override
    public void saveAll(List<TransactionB> transactionList) {
        repository.saveAll(transactionList);
    }
}
