package com.exchange.matching.domain.service;

import com.exchange.matching.application.dto.enums.OrderStatus;
import com.exchange.matching.application.dto.enums.OrderType;
import com.exchange.matching.domain.entiry.Order;
import com.exchange.matching.domain.entiry.TransactionB;
import com.exchange.matching.domain.repository.TransactionBReader;
import com.exchange.matching.domain.repository.TransactionBStore;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MatchingServiceV1B {

    private final TransactionBReader transactionReader;
    private final TransactionBStore transactionStore;
    private static final int MAX_RETRIES = 3;

    @Transactional
    public void matchOrders(Order order) {
        // 매수/구매(BUY)인지 매도/판매(SELL)인지에 따라 반대 주문 타입 결정
        OrderType oppositeType = (order.getOrderType() == OrderType.BUY) ? OrderType.SELL : OrderType.BUY;

        // 낙관적 락 버전 충돌 시 증가
        int attempts = 0;

        while (attempts < MAX_RETRIES && order.getQuantity().compareTo(BigDecimal.ZERO) > 0) {
            try {
                // 반대 주문 조회
                Optional<TransactionB> topOppositeOrderOpt = findTopOppositeOrder(order, oppositeType);

                // 조회된 주문이 없거나, 가격 조건이 맞지 않으면 미체결(부분 체결) 상태(PENDING)로 저장
                if (!topOppositeOrderOpt.isPresent() || !isPriceMatch(order, topOppositeOrderOpt.get())) {
                    // 매칭 실패
                    saveTransaction(order);
                    return;
                }

                // 매칭 성공
                // 주문 체결 수행
                boolean executed = executeMatching(order, topOppositeOrderOpt.get());

                // 완전 체결된 경우
                if (executed) {
                    saveTransaction(order);
                    break;
                }
            } catch (OptimisticLockException | OptimisticLockingFailureException e) {
                // 예외 발생 시 시도 횟수 증가시키고 재시도
                attempts += 1;
                if (attempts >= MAX_RETRIES) {
                    // 최대 재시도 횟수 초과 시 미체결 상태로 저장
                    saveTransaction(order);
                    break;
                }
            }
        }
    }

    // 1. 반대(매칭 대상) 주문 조회
    // BUY 주문이면 SELL 주문을, SELL 주문이면 BUY 주문을 조회
    private Optional<TransactionB> findTopOppositeOrder(Order order, OrderType oppositeType) {
        if (order.getOrderType() == OrderType.BUY) return transactionReader.findTopByTypeAndTradingPairAndStatusOrderByPriceAscCreatedAtAsc(
                oppositeType, order.getTradingPair(), OrderStatus.PENDING);
        // SELL 주문인 경우
        else return transactionReader.findTopByTypeAndTradingPairAndStatusOrderByPriceDescCreatedAtAsc(
                oppositeType, order.getTradingPair(), OrderStatus.PENDING);
    }

    // 2. 주문 가격 매칭 조건 확인
    // BUY 주문이면 판매 주문의 가격이 BUY 주문의 가격 이하여야 하고,
    // SELL 주문이면 구매 주문의 가격이 SELL 주문의 가격 이상이어야 함
    private boolean isPriceMatch(Order order, TransactionB oppositeOrder) {
        // 판매 주문의 가격이 BUY 주문 가격보다 낮거나 같으면 매칭 가능
        if (order.getOrderType() == OrderType.BUY) return oppositeOrder.getPrice().compareTo(order.getPrice()) <= 0;
        // SELL 주문인 경우, 구매 주문의 가격이 SELL 주문 가격보다 높거나 같으면 매칭 가능
        else return oppositeOrder.getPrice().compareTo(order.getPrice()) >= 0;
    }

    // 3. 양쪽 주문에 대해 체결된 수량 계산 및 상태 업데이트
    private boolean executeMatching(Order order, TransactionB oppositeOrder) {
        // 주문 간 수량의 최솟값 선택하여 체결 수량 결정
        BigDecimal matchedQuantity = order.getQuantity().min(oppositeOrder.getQuantity());

        // 주문 간 체결 수량 감소
        order.setQuantity(order.getQuantity().subtract(matchedQuantity));
        oppositeOrder.setQuantity(oppositeOrder.getQuantity().subtract(matchedQuantity));

        // 주문의 완전 체결 여부 업데이트
        if (order.getQuantity().compareTo(BigDecimal.ZERO) == 0) order.setOrderStatus(OrderStatus.COMPLETED);
        if (oppositeOrder.getQuantity().compareTo(BigDecimal.ZERO) == 0) oppositeOrder.setStatus(OrderStatus.COMPLETED);

        // 요청 주문이 완전 체결된 경우 true 반환
        return order.getOrderStatus() == OrderStatus.COMPLETED;
    }

    // 4. 체결/미체결 주문 거래 내역을 DB에 저장
    private void saveTransaction(Order order) {
        TransactionB transaction = TransactionB.builder()
                .userId(order.getUserId())
                .tradingPair(order.getTradingPair())
                .price(order.getPrice())
                .quantity(order.getQuantity())
                .type(order.getOrderType())
                .status(order.getOrderStatus())
                .build();
        transactionStore.save(transaction);
    }
}
