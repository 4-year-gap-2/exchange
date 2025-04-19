package com.exchange.order_completed.application.service;

import com.exchange.order_completed.application.command.CreateOrderStoreCommand;
import com.exchange.order_completed.domain.entiry.CompletedOrder;
import com.exchange.order_completed.domain.repository.CompletedOrderStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderCompletedFacade {

    private final CompletedOrderStore completedOrderStore;

    public void saveCompletedOrder(CreateOrderStoreCommand command) {
        CompletedOrder completedOrder = command.toEntity();
        completedOrderStore.save(completedOrder);
    }
}
