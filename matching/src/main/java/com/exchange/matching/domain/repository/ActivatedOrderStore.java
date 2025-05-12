package com.exchange.matching.domain.repository;

import com.exchange.matching.domain.entity.UnmatchedOrderA;

import java.util.List;

public interface ActivatedOrderStore {

    void save(UnmatchedOrderA unmatchedOrderA);

    void delete(UnmatchedOrderA unmatchedOrderA);

    void deleteAll();

    void saveAll(List<UnmatchedOrderA> unmatchedOrderAList);
}
