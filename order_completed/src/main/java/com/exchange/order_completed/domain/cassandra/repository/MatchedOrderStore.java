package com.exchange.order_completed.domain.cassandra.repository;

import com.exchange.order_completed.domain.cassandra.entity.MatchedOrder;
import com.exchange.order_completed.domain.cassandra.entity.UnmatchedOrder;

import java.util.List;

public interface MatchedOrderStore {

    void saveBatch(List<MatchedOrder> matchedOrderList);

    void save(MatchedOrder matchedOrder);

    void saveMatchedOrderAndUpdateUnmatchedOrder(MatchedOrder matchedOrder, UnmatchedOrder unmatchedOrder);

    void deleteAll();
}
