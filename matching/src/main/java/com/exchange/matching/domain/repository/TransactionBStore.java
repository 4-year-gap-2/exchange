package com.exchange.matching.domain.repository;

import com.exchange.matching.domain.entiry.TransactionB;

import java.util.List;

public interface TransactionBStore {

    void save(TransactionB transaction);

    void deleteAll();

    void saveAll(List<TransactionB> transactionList);
}
