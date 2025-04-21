package com.exchange.order_completed.application.service;

import com.exchange.order_completed.application.command.CreateOrderStoreCommand;
import com.exchange.order_completed.domain.entiry.CompletedOrder;
import com.exchange.order_completed.domain.repository.CompletedOrderReader;
import com.exchange.order_completed.domain.repository.CompletedOrderStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderCompletedFacade {

    private final CompletedOrderStore completedOrderStore;
    private final CompletedOrderReader completedOrderReader;

    public void saveCompletedOrder(CreateOrderStoreCommand command) {
        CompletedOrder persistedCompletedOrder = completedOrderReader.findByUserIdAndOrderId(command.userId(), command.orderId());

        if (persistedCompletedOrder != null) {
            log.info("이미 완료된 주문입니다. orderId: {}", command.orderId());
            return;
        }

        CompletedOrder newCompletedOrder = command.toEntity();
        completedOrderStore.save(newCompletedOrder);
    }
}
