package com.exchange.matching.domain.repository;

import com.exchange.matching.domain.entity.UnmatchedOrderB;

import java.util.List;

public interface ActivatedOrderBStore {

    void delete(UnmatchedOrderB oppositeOrder);

    void save(UnmatchedOrderB activatedOrder);

    void deleteAll();

    void saveAll(List<UnmatchedOrderB> unmatchedOrderBList);
}
