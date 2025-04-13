package com.exchange.matching.domain.service;

import com.exchange.matching.application.dto.enums.OrderStatus;
import com.exchange.matching.application.dto.enums.OrderType;
import com.exchange.matching.domain.entiry.Order;
import com.exchange.matching.domain.entiry.Transaction;
import com.exchange.matching.domain.repository.TransactionReader;
import com.exchange.matching.domain.repository.TransactionStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MatchingServiceV1A {

    private final TransactionReader transactionReader;
    private final TransactionStore transactionStore;

    @Transactional
    public void matchOrders(Order order) {
        // 매수 주문 처리
        if (order.getOrderType() == OrderType.BUY) {
            while (order.getQuantity().compareTo(BigDecimal.ZERO) > 0) {
                Optional<Transaction> top = transactionReader.findTopByTypeAndTradingPairAndStatusOrderByPriceAscCreatedAtAsc(
                        OrderType.SELL, order.getTradingPair(), OrderStatus.PENDING
                );

                // 매수 주문과 매도 주문의 가격 비교
                boolean matched = matchBuyOrders(order, top.orElse(null));
                if (matched) {
                    // 매칭 성공
                    // 체결 처리
                    boolean executed = executeBuyOrders(order, top.get());
                    if (executed) {
                        // 완전 체결된 주문의 거래 내역 저장
                        saveTransaction(order);
                        break;
                    }
                } else {
                    // 매칭 실패
                    // 미체결 주문으로 저장
                    saveTransaction(order);
                    break;
                }
            }
        }
        // 매도 주문 처리
        else if (order.getOrderType() == OrderType.SELL) {
            while (order.getQuantity().compareTo(BigDecimal.ZERO) > 0) {
                Optional<Transaction> top = transactionReader.findTopByTypeAndTradingPairAndStatusOrderByPriceDescCreatedAtAsc(
                        OrderType.BUY, order.getTradingPair(), OrderStatus.PENDING
                );

                // 매도 주문과 매수 주문의 가격 비교
                boolean matched = matchSellOrders(order, top.orElse(null));
                if (matched) {
                    // 매칭 성공
                    // 체결 처리
                    boolean executed = executeSellOrders(order, top.get());
                    if (executed) {
                        // 완전 체결된 주문의 거래 내역 저장
                        saveTransaction(order);
                        break;
                    }
                } else {
                    // 매칭 실패
                    // 미체결 주문으로 저장
                    saveTransaction(order);
                    break;
                }
            }
        }
    }

    // 1. 매수 주문과 매도 주문의 가격 비교
    private boolean matchBuyOrders(Order buyOrder, Transaction sellOrder) {
        if (sellOrder == null) return false;    // 매도 주문이 없으면 매칭 불가
        if (sellOrder.getPrice().compareTo(buyOrder.getPrice()) > 0) return false;  // 매도 주문의 가격이 매수 주문의 가격보다 높으면 매칭 불가

        return true;
    }

    private boolean matchSellOrders(Order sellOrder, Transaction buyOrder) {
        if (buyOrder == null) return false;    // 매수 주문이 없으면 매칭 불가
        if (buyOrder.getPrice().compareTo(sellOrder.getPrice()) < 0) return false;  // 매수 주문의 가격이 매도 주문의 가격보다 낮으면 매칭 불가

        return true;
    }

    // 2. 가격이 맞으면 체결 처리
    private boolean executeBuyOrders(Order buyOrder, Transaction sellOrder) {
        // 체결된 주문의 수량 계산
        // 매수 주문의 수량과 매도 주문의 수량 중 작은 값을 선택
        BigDecimal matchedQuantity = buyOrder.getQuantity().min(sellOrder.getQuantity());

        // 체결된 주문의 수량 업데이트
        // 매수 주문의 수량에서 체결된 수량을 빼고, 매도 주문의 수량에서 체결된 수량을 뺌
        buyOrder.setQuantity(buyOrder.getQuantity().subtract(matchedQuantity));
        sellOrder.setQuantity(sellOrder.getQuantity().subtract(matchedQuantity));

        // 매수 주문이 모두 체결된 경우
        if (buyOrder.getQuantity().compareTo(BigDecimal.ZERO) == 0) buyOrder.setOrderStatus(OrderStatus.COMPLETED);
        // 매도 주문이 모두 체결된 경우
        if (sellOrder.getQuantity().compareTo(BigDecimal.ZERO) == 0) sellOrder.setStatus(OrderStatus.COMPLETED);

        return buyOrder.getOrderStatus() == OrderStatus.COMPLETED;
    }

    private boolean executeSellOrders(Order sellOrder, Transaction buyOrder) {
        // 체결된 주문의 수량 계산
        // 매도 주문의 수량과 매수 주문의 수량 중 작은 값을 선택
        BigDecimal matchedQuantity = sellOrder.getQuantity().min(buyOrder.getQuantity());

        // 체결된 주문의 수량 업데이트
        // 매도 주문의 수량에서 체결된 수량을 빼고, 매수 주문의 수량에서 체결된 수량을 뺌
        sellOrder.setQuantity(sellOrder.getQuantity().subtract(matchedQuantity));
        buyOrder.setQuantity(buyOrder.getQuantity().subtract(matchedQuantity));

        // 매도 주문이 모두 체결된 경우
        if (sellOrder.getQuantity().compareTo(BigDecimal.ZERO) == 0) sellOrder.setOrderStatus(OrderStatus.COMPLETED);
        // 매수 주문이 모두 체결된 경우
        if (buyOrder.getQuantity().compareTo(BigDecimal.ZERO) == 0) buyOrder.setStatus(OrderStatus.COMPLETED);

        return sellOrder.getOrderStatus() == OrderStatus.COMPLETED;
    }

    // 3. 체결/미체결 주문 거래 내역을 DB에 저장
    private void saveTransaction(Order order) {
        Transaction transaction = Transaction.builder()
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
