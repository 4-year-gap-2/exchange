package com.exchange.order_completed.domain.repository;

import com.exchange.order_completed.application.command.CreateTestOrderStoreCommand;
import com.exchange.order_completed.domain.cassandra.entity.MatchedOrder;
import com.exchange.order_completed.domain.cassandra.entity.UnmatchedOrder;

public interface MatchedOrderStore {

    void save(MatchedOrder matchedOrder);

    void saveMatchedOrderAndUpdateUnmatchedOrder(MatchedOrder matchedOrder, UnmatchedOrder unmatchedOrder);

    void deleteAll();

    void saveBatch(CreateTestOrderStoreCommand command);
}
