package com.exchange.matching.domain.repository;

import com.exchange.matching.domain.entiry.Transaction;

import java.util.List;

public interface TransactionStore {

    void save(Transaction transaction);

    void deleteAll();

    void saveAll(List<Transaction> transactionList);
}
