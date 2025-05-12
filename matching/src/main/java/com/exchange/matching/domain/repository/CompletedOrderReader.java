package com.exchange.matching.domain.repository;

import com.exchange.matching.domain.entity.MatchedOrder;

import java.util.List;

public interface CompletedOrderReader {

    List<MatchedOrder> findAll();
}
