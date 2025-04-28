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

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class OrderCompletedService {

    private final MatchedOrderStore matchedOrderStore;
    private final MatchedOrderReader matchedOrderReader;
    private final UnmatchedOrderReader unmatchedOrderReader;
    private final UnmatchedOrderStore unmatchedOrderStore;

    public void completeMatchedOrder(CreateOrderStoreCommand command, Integer attempt) {
        MatchedOrder persistentMatchedOrder = matchedOrderReader.findByUserIdAndOrderId(command.userId(), command.orderId(), attempt);

        if (persistentMatchedOrder != null) {
            throw new DuplicateMatchedOrderInformationException("이미 저장된 체결 주문입니다. orderId: " + command.orderId());
        }

        MatchedOrder newMatchedOrder = command.toMatchedOrderEntity();
        UnmatchedOrder persistentUnmatchedOrder = unmatchedOrderReader.findUnmatchedOrder(command.userId(), command.orderId(), attempt);

        if (persistentUnmatchedOrder == null) {
            matchedOrderStore.save(newMatchedOrder);
        } else {
            updateUnmatchedOrderQuantity(newMatchedOrder, persistentUnmatchedOrder);
            // 카산드라 배치 쿼리 수행
            matchedOrderStore.saveMatchedOrderAndUpdateUnmatchedOrder(newMatchedOrder, persistentUnmatchedOrder);
        }
    }

    public void updateUnmatchedOrderQuantity(MatchedOrder matchedOrder, UnmatchedOrder unmatchedOrder) {
        BigDecimal matchedOrderQuantity = matchedOrder.getQuantity();
        BigDecimal unmatchedOrderQuantity = unmatchedOrder.getQuantity();

        // 체결된 주문의 수량이 미체결 주문의 수량보다 적은 경우
        if (matchedOrderQuantity.compareTo(unmatchedOrderQuantity) < 0) {
            // 체결된 주문의 수량을 미체결 주문의 수량에서 빼줌
            unmatchedOrder.setQuantity(unmatchedOrderQuantity.subtract(matchedOrderQuantity));

            // 체결된 주문의 수량이 미체결 주문의 수량과 같은 경우
        } else if (matchedOrderQuantity.compareTo(unmatchedOrderQuantity) == 0) {
            // 미체결 주문의 수량을 0으로 설정
            unmatchedOrder.setQuantity(BigDecimal.ZERO);
        }
    }

    public void completeUnmatchedOrder(CreateOrderStoreCommand command, Integer attempt) {
        UnmatchedOrder persistentMatchedOrder = unmatchedOrderReader.findUnmatchedOrder(command.userId(), command.orderId(), attempt);

        if (persistentMatchedOrder != null) {
            throw new DuplicateUnmatchedOrderInformationException("이미 저장된 미체결 주문입니다. orderId: " + command.orderId());
        }

        UnmatchedOrder newUnmatchedOrder = command.toUnmatchedOrderEntity();
        unmatchedOrderStore.save(newUnmatchedOrder);
    }
}
