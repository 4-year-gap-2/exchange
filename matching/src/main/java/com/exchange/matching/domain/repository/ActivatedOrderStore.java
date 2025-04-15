package com.exchange.matching.domain.repository;

import com.exchange.matching.domain.entiry.ActivatedOrder;

import java.util.List;

public interface ActivatedOrderStore {

    void save(ActivatedOrder activatedOrder);

    void delete(ActivatedOrder activatedOrder);

    void deleteAll();

    void saveAll(List<ActivatedOrder> activatedOrderList);
}
