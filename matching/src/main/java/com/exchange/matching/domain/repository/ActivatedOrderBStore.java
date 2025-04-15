package com.exchange.matching.domain.repository;

import com.exchange.matching.domain.entiry.ActivatedOrderB;

import java.util.List;

public interface ActivatedOrderBStore {

    void delete(ActivatedOrderB oppositeOrder);

    void save(ActivatedOrderB activatedOrder);

    void deleteAll();

    void saveAll(List<ActivatedOrderB> activatedOrderBList);
}
