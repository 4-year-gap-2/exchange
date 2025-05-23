package com.exchange.matching.domain.service;

import com.exchange.matching.application.command.CreateMatchingCommand;
import com.exchange.matching.application.enums.MatchingVersion;
import com.exchange.matching.application.enums.OrderType;
import com.exchange.matching.domain.entity.MatchedOrder;
import com.exchange.matching.domain.entity.UnmatchedOrderA;
import com.exchange.matching.domain.repository.*;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MatchingServiceV1A implements MatchingService {

    private final ActivatedOrderReader activatedOrderReader;
    private final ActivatedOrderStore activatedOrderStore;
    private final CompletedOrderStore completedOrderStore;

    @Override
    public MatchingVersion getVersion() {
        return MatchingVersion.V1A;
    }

    @Override
    @Transactional
    public void matchOrders(CreateMatchingCommand command) {
        // mutable 객체로 변환
        Order order = Order.from(command);

        // 매수/구매(BUY)인지 매도/판매(SELL)인지에 따라 반대 주문 타입 결정
        OrderType oppositeType = (order.getOrderType() == OrderType.BUY) ? OrderType.SELL : OrderType.BUY;

        while (order.getQuantity().compareTo(BigDecimal.ZERO) > 0) {
            // 반대 주문 조회
            Optional<UnmatchedOrderA> topOppositeOrderOpt = findTopOppositeOrder(order, oppositeType);

            if (!topOppositeOrderOpt.isPresent() || !isPriceMatch(order, topOppositeOrderOpt.get())) {
                // 매칭 실패
                // 조회된 주문이 없거나, 가격 조건이 맞지 않으면 미체결(부분 체결) 상태로 저장
                saveActivatedOrder(order);
                return;
            }

            // 매칭 성공
            // 주문 체결 수행
            boolean fullyExecuted = executeMatching(order, topOppositeOrderOpt.get());
            // 요청된 주문이 완전 체결된 경우
            if (fullyExecuted) break;
        }
    }

    // 1. 반대(매칭 대상) 주문 조회
    // BUY 주문이면 SELL 주문을, SELL 주문이면 BUY 주문을 조회
    private Optional<UnmatchedOrderA> findTopOppositeOrder(Order order, OrderType oppositeType) {
        // 요청 주문이 BUY 주문이므로 SELL 주문을 조회
        if (order.getOrderType() == OrderType.BUY) return activatedOrderReader.findTopByTypeAndTradingPairOrderByPriceAscCreatedAtAsc(
                oppositeType, order.getTradingPair());
            // 요청 주문이 SELL 주문이므로 BUY 주문을 조회
        else return activatedOrderReader.findTopByTypeAndTradingPairOrderByPriceDescCreatedAtAsc(
                oppositeType, order.getTradingPair());
    }

    // 2. 주문 가격 매칭 조건 확인
    // BUY 주문이면 판매 주문의 가격이 BUY 주문의 가격 이하여야 하고,
    // SELL 주문이면 구매 주문의 가격이 SELL 주문의 가격 이상이어야 함
    private boolean isPriceMatch(Order order, UnmatchedOrderA oppositeOrder) {
        // 판매 주문의 가격이 BUY 주문 가격보다 낮거나 같으면 매칭 가능
        if (order.getOrderType() == OrderType.BUY) return oppositeOrder.getPrice().compareTo(order.getPrice()) <= 0;
            // SELL 주문인 경우, 구매 주문의 가격이 SELL 주문 가격보다 높거나 같으면 매칭 가능
        else return oppositeOrder.getPrice().compareTo(order.getPrice()) >= 0;
    }

    // 3. 양쪽 주문에 대해 체결된 수량 계산 및 상태 업데이트
    private boolean executeMatching(Order order, UnmatchedOrderA oppositeOrder) {
        // 주문 간 수량의 최솟값 선택하여 체결 수량 결정
        BigDecimal matchedQuantity = order.getQuantity().min(oppositeOrder.getQuantity());

        // 주문 간 체결 수량 감소
        order.setQuantity(order.getQuantity().subtract(matchedQuantity));
        oppositeOrder.setQuantity(oppositeOrder.getQuantity().subtract(matchedQuantity));

        // 체결된 만큼 거래 내역 저장
        saveCompletedOrder(order, matchedQuantity, oppositeOrder.getPrice(), oppositeOrder.getUserId());
        saveCompletedOrder(oppositeOrder, matchedQuantity, oppositeOrder.getPrice(), order.getUserId());

        if (oppositeOrder.getQuantity().compareTo(BigDecimal.ZERO) == 0) {
            // 반대 주문은 DB에 저장되어 있기 때문에 반대 주문이 완전 체결된 경우 DB에서 삭제
            activatedOrderStore.delete(oppositeOrder);
        }
        if (order.getQuantity().compareTo(BigDecimal.ZERO) == 0) {
            // 요청 주문이 완전 체결된 경우
            return true;
        }

        return false;
    }

    // 4-1. 미체결 주문 거래 내역을 DB에 저장
    private void saveActivatedOrder(Order order) {
        UnmatchedOrderA unmatchedOrderA = UnmatchedOrderA.builder()
                .userId(order.getUserId())
                .orderId(order.getOrderId())
                .tradingPair(order.getTradingPair())
                .price(order.getPrice())
                .quantity(order.getQuantity())
                .type(order.getOrderType())
                .createdAt(LocalDateTime.now())
                .build();
        activatedOrderStore.save(unmatchedOrderA);
    }

    // 4-2. 체결된 주문 거래 내역을 DB에 저장
    private void saveCompletedOrder(Order order, BigDecimal matchedQuantity, BigDecimal matchedPrice, UUID oppositeOrderUserId) {
        MatchedOrder matchedOrder = MatchedOrder.builder()
                .sellerId(order.getOrderType() == OrderType.SELL ? order.getUserId() : oppositeOrderUserId)
                .buyerId(order.getOrderType() == OrderType.BUY ? order.getUserId() : oppositeOrderUserId)
                .orderId(order.getOrderId())
                .tradingPair(order.getTradingPair())
                .price(matchedPrice)
                .quantity(matchedQuantity)
                .type(order.getOrderType())
                .createdAt(LocalDateTime.now())
                .build();
        completedOrderStore.save(matchedOrder);
    }

    private void saveCompletedOrder(UnmatchedOrderA order, BigDecimal matchedQuantity, BigDecimal matchedPrice, UUID oppositeOrderUserId) {
        MatchedOrder matchedOrder = MatchedOrder.builder()
                .sellerId(order.getType() == OrderType.SELL ? order.getUserId() : oppositeOrderUserId)
                .buyerId(order.getType() == OrderType.BUY ? order.getUserId() : oppositeOrderUserId)
                .orderId(order.getOrderId())
                .tradingPair(order.getTradingPair())
                .price(matchedPrice)
                .quantity(matchedQuantity)
                .type(order.getType())
                .createdAt(LocalDateTime.now())
                .build();
        completedOrderStore.save(matchedOrder);
    }

    @Getter
    @Setter
    @Builder
    public static class Order {

        private String tradingPair;         // ("BTC/KRW")
        private OrderType orderType;        // (BUY, SELL)
        private BigDecimal price;
        private BigDecimal quantity;
        private UUID userId;
        private UUID orderId;

        public static Order from(CreateMatchingCommand createMatchingCommand) {
            return Order.builder()
                    .tradingPair(createMatchingCommand.tradingPair())
                    .orderType(createMatchingCommand.orderType())
                    .price(createMatchingCommand.price())
                    .quantity(createMatchingCommand.quantity())
                    .userId(createMatchingCommand.userId())
                    .orderId(createMatchingCommand.orderId())
                    .build();
        }
    }
}


