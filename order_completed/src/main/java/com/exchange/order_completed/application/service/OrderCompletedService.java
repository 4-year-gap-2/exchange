package com.exchange.order_completed.application.service;

import com.exchange.order_completed.application.command.ChartCommand;
import com.exchange.order_completed.application.command.CreateMatchedOrderStoreCommand;
import com.exchange.order_completed.application.command.CreateTestOrderStoreCommand;
import com.exchange.order_completed.application.command.CreateUnmatchedOrderStoreCommand;
import com.exchange.order_completed.common.exception.DuplicateMatchedOrderInformationException;
import com.exchange.order_completed.common.exception.DuplicateUnmatchedOrderInformationException;
import com.exchange.order_completed.domain.entity.MatchedOrder;
import com.exchange.order_completed.domain.entity.UnmatchedOrder;
import com.exchange.order_completed.domain.postgresEntity.Chart;
import com.exchange.order_completed.domain.repository.MatchedOrderReader;
import com.exchange.order_completed.domain.repository.MatchedOrderStore;
import com.exchange.order_completed.domain.repository.UnmatchedOrderReader;
import com.exchange.order_completed.domain.repository.UnmatchedOrderStore;
import com.exchange.order_completed.infrastructure.postgesql.repository.ChartRepositoryStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class OrderCompletedService {

    private final MatchedOrderStore matchedOrderStore;
    private final MatchedOrderReader matchedOrderReader;
    private final UnmatchedOrderReader unmatchedOrderReader;
    private final UnmatchedOrderStore unmatchedOrderStore;
    private final ChartRepositoryStore chartRepositoryStore;

    public void completeOrderEach(CreateTestOrderStoreCommand command, Integer attempt) {
        MatchedOrder buyOrderEntity = command.toBuyOrderEntity();
        MatchedOrder sellOrderEntity = command.toSellOrderEntity();
        matchedOrderStore.save(buyOrderEntity);
        matchedOrderStore.save(sellOrderEntity);
    }
    public void completeOrderBatch(CreateTestOrderStoreCommand command, Integer attempt) {
        matchedOrderStore.saveBatch(command);
    }

    public void completeMatchedOrder(CreateMatchedOrderStoreCommand command, Integer attempt) {
//        MatchedOrder persistentMatchedOrder = matchedOrderReader.findMatchedOrder(command.userId(), command.idempotencyId(), attempt);
//
//        if (persistentMatchedOrder != null) {
//            throw new DuplicateMatchedOrderInformationException("이미 저장된 체결 주문입니다. orderId: " + command.idempotencyId());
//        }

        MatchedOrder newMatchedOrder = command.toEntity();
//        UnmatchedOrder persistentUnmatchedOrder = unmatchedOrderReader.findUnmatchedOrder(command.userId(), command.orderId(), attempt);
//
//        if (persistentUnmatchedOrder == null) {
            matchedOrderStore.save(newMatchedOrder);
//        } else {
//            updateUnmatchedOrderQuantity(newMatchedOrder, persistentUnmatchedOrder);
//            // 카산드라 배치 쿼리 수행
//            matchedOrderStore.saveMatchedOrderAndUpdateUnmatchedOrder(newMatchedOrder, persistentUnmatchedOrder);
//        }
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

    public void completeUnmatchedOrder(CreateUnmatchedOrderStoreCommand command, Integer attempt) {
//        UnmatchedOrder persistentMatchedOrder = unmatchedOrderReader.findUnmatchedOrder(command.userId(), command.orderId(), attempt);
//
//        if (persistentMatchedOrder != null) {
//            throw new DuplicateUnmatchedOrderInformationException("이미 저장된 미체결 주문입니다. orderId: " + command.orderId());
//        }

        UnmatchedOrder newUnmatchedOrder = command.toEntity();
        unmatchedOrderStore.save(newUnmatchedOrder);
    }

    @Transactional
    public void saveChart(ChartCommand command) {
        Chart chart = Chart.from(command);
        chartRepositoryStore.save(chart);
    }
}
