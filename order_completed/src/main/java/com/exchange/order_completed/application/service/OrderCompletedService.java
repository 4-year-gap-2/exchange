package com.exchange.order_completed.application.service;

import com.exchange.order_completed.application.command.ChartCommand;
import com.exchange.order_completed.application.command.CreateOrderStoreCommand;
import com.exchange.order_completed.common.exception.DuplicateOrderCompletionException;
import com.exchange.order_completed.domain.entity.CompletedOrder;
import com.exchange.order_completed.domain.postgresEntity.Chart;
import com.exchange.order_completed.domain.repository.CompletedOrderReader;
import com.exchange.order_completed.domain.repository.CompletedOrderStore;
import com.exchange.order_completed.infrastructure.postgesql.repository.ChartRepositoryStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderCompletedService {

    private final CompletedOrderStore completedOrderStore;
    private final CompletedOrderReader completedOrderReader;
    private final ChartRepositoryStore chartRepositoryStore;

    public void completeOrder(CreateOrderStoreCommand command, Integer attempt) {
        CompletedOrder persistentOrder = completedOrderReader.findByUserIdAndOrderId(command.userId(), command.orderId(), attempt);

        if (persistentOrder != null) {
            throw new DuplicateOrderCompletionException("이미 완료된 주문입니다. orderId: " + command.orderId());
        }

        CompletedOrder newCompletedOrder = command.toEntity();
        completedOrderStore.save(newCompletedOrder);
    }

    @Transactional
    public void saveChart(ChartCommand command) {
        Chart chart = Chart.from(command);
        chartRepositoryStore.save(chart);
    }
}
