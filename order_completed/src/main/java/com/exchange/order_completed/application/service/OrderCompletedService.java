package com.exchange.order_completed.application.service;

import com.exchange.order_completed.application.command.CreateOrderStoreCommand;
import com.exchange.order_completed.common.exception.DuplicateOrderCompletionException;
import com.exchange.order_completed.domain.entity.MatchedOrder;
import com.exchange.order_completed.domain.postgresEntity.Chart;
import com.exchange.order_completed.domain.repository.MatchedOrderReader;
import com.exchange.order_completed.domain.repository.MatchedOrderStore;
import com.exchange.order_completed.infrastructure.postgesql.repository.ChartRepositoryStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderCompletedService {

    private final MatchedOrderStore matchedOrderStore;
    private final MatchedOrderReader matchedOrderReader;
    private final ChartRepositoryStore chartRepositoryStore;

    public void completeOrder(CreateOrderStoreCommand command, Integer attempt) {
        MatchedOrder persistentOrder = matchedOrderReader.findByUserIdAndOrderId(command.userId(), command.orderId(), attempt);

        if (persistentOrder != null) {
            throw new DuplicateOrderCompletionException("이미 완료된 주문입니다. orderId: " + command.orderId());
        }

        MatchedOrder newMatchedOrder = command.toEntity();
        Chart chart = command.toChartData();
        matchedOrderStore.save(newMatchedOrder);
        chartRepositoryStore.save(chart);
    }
}
