package com.exchange.order_completed.application.service;

import com.exchange.order_completed.application.command.CreateOrderStoreCommand;
import com.exchange.order_completed.common.exception.DuplicateMatchedOrderInformationException;
import com.exchange.order_completed.common.exception.DuplicateUnmatchedOrderInformationException;
import com.exchange.order_completed.domain.entity.MatchedOrder;
import com.exchange.order_completed.domain.entity.UnmatchedOrder;
import com.exchange.order_completed.domain.repository.MatchedOrderReader;
import com.exchange.order_completed.domain.repository.MatchedOrderStore;
import com.exchange.order_completed.domain.repository.UnmatchedOrderReader;
import com.exchange.order_completed.domain.repository.UnmatchedOrderStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderCompletedService {

    private final MatchedOrderStore matchedOrderStore;
    private final MatchedOrderReader matchedOrderReader;
    private final UnmatchedOrderReader unmatchedOrderReader;
    private final UnmatchedOrderStore unmatchedOrderStore;

    public void completeMatchedOrder(CreateOrderStoreCommand command, Integer attempt) {
        MatchedOrder persistentOrder = matchedOrderReader.findByUserIdAndOrderId(command.userId(), command.orderId(), attempt);

        if (persistentOrder != null) {
            throw new DuplicateMatchedOrderInformationException("이미 저장된 체결 주문입니다. orderId: " + command.orderId());
        }

        MatchedOrder newMatchedOrder = command.toMatchedOrderEntity();
        matchedOrderStore.save(newMatchedOrder);
    }

    public void completeUnmatchedOrder(CreateOrderStoreCommand command, Integer attempt) {
        UnmatchedOrder persistentOrder = unmatchedOrderReader.findByUserIdAndOrderId(command.userId(), command.orderId(), attempt);

        if (persistentOrder != null) {
            throw new DuplicateUnmatchedOrderInformationException("이미 저장된 미체결 주문입니다. orderId: " + command.orderId());
        }

        UnmatchedOrder newUnmatchedOrder = command.toUnmatchedOrderEntity();
        unmatchedOrderStore.save(newUnmatchedOrder);
    }
}
