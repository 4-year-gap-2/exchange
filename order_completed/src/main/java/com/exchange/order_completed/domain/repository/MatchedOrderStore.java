package com.exchange.order_completed.domain.repository;

import com.exchange.order_completed.application.command.CreateTestOrderStoreCommand;
import com.exchange.order_completed.domain.entity.MatchedOrder;
import com.exchange.order_completed.domain.entity.UnmatchedOrder;

public interface MatchedOrderStore {

    void save(MatchedOrder matchedOrder);

    void saveMatchedOrderAndUpdateUnmatchedOrder(MatchedOrder matchedOrder, UnmatchedOrder unmatchedOrder);

    void deleteAll();

    void save(CreateTestOrderStoreCommand command);
}
