package com.exchange.matching.application.service;

import com.exchange.matching.application.command.CreateMatchingCommand;
import com.exchange.matching.application.dto.enums.OrderType;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;


import java.math.BigDecimal;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Slf4j
public class MatchingServiceV2 implements MatchingService {

    private final RedisTemplate<String, String> redisTemplate;

    @Override
    @Transactional
    public void matchOrders(CreateMatchingCommand command) {
        // 카프카에서 값 읽기 토픽은 [4yearGap.order.orderEvent.match]

        V2MatchOrder v2MatchOrder = V2MatchOrder.fromCommand(command);
        matchingProcess(v2MatchOrder);
    }

    private void matchingProcess(V2MatchOrder incomingOrder) {
        BigDecimal remainingQuantity = incomingOrder.getQuantity();
        while (remainingQuantity.compareTo(BigDecimal.ZERO) != 0) {
            V2MatchOrder matchedOrder = findMatchingOrder(incomingOrder.getTradingPair(), incomingOrder.getOrderType());
            if (matchedOrder == null) {
                saveOrderToRedis(updateOrderQuantity(incomingOrder, remainingQuantity));
                return;
            }

            BigDecimal matchedQuantity = matchedOrder.getQuantity();
            // 현재 거래 가능한 미체결 주문이 있다
            if (incomingOrder.getOrderType().equals(OrderType.SELL) && matchedOrder.getPrice().compareTo(incomingOrder.getPrice()) >= 0 ||
                    incomingOrder.getOrderType().equals(OrderType.BUY) && matchedOrder.getPrice().compareTo(incomingOrder.getPrice()) <= 0) {
                // 미체결 주문 수량 보다 주문한 수량이 많으면
                // 미체결 주문 삭제
                if (remainingQuantity.compareTo(matchedQuantity) >= 0) {
                    removeOrderFromRedis(matchedOrder);
                    remainingQuantity = remainingQuantity.subtract(matchedQuantity);
                    log.info("체결 남은 주분 수량 {} 완전 체결 수량 {}", remainingQuantity, incomingOrder.getQuantity());
                } else {
                    // 미체결 주문 수량 보다 주문한 수량이 적으면
                    // 미체결 주문 수랭 차감 후 저장
                    removeOrderFromRedis(matchedOrder);
                    V2MatchOrder updateMatchedOrder = updateOrderQuantity(matchedOrder, matchedOrder.getQuantity().subtract(remainingQuantity));
                    resaveOrderToRedis(updateMatchedOrder);
                    remainingQuantity = BigDecimal.ZERO;
                    log.info("체결 남은 주분 수량 {} 체결 수량 {}", remainingQuantity, matchedOrder.getQuantity());
                }
            } else {
                saveOrderToRedis(updateOrderQuantity(incomingOrder, remainingQuantity));
                return;
            }
        }
    }

    private V2MatchOrder findMatchingOrder(String stockCode, OrderType orderType) {
        String key = orderType.equals(OrderType.SELL) ? "kj_buy_orders:" + stockCode : "kj_sell_orders:" + stockCode;

        ZSetOperations<String, String> zSetOperations = redisTemplate.opsForZSet();
        ZSetOperations.TypedTuple<String> strOrder = null;
        if (orderType.equals(OrderType.SELL)) {
            strOrder = zSetOperations.reverseRangeWithScores(key, 0, 0).stream().findFirst().orElse(null);
        } else {
            strOrder = zSetOperations.rangeWithScores(key, 0, 0).stream().findFirst().orElse(null);
        }

        if (null == strOrder) {
            return null;
        }
        V2MatchOrder matchingCommand = deserializeOrder(strOrder.getValue(), stockCode,orderType.equals(OrderType.SELL) ? OrderType.BUY:OrderType.SELL , strOrder.getScore());

        return matchingCommand;

    }

    private V2MatchOrder deserializeOrder(String strOrder, String stockCode, OrderType orderType, double score) {

        String[] parts = strOrder.split("\\|");

        double quantity = Double.parseDouble(parts[0]);

        BigDecimal scoreAsBigDecimal = BigDecimal.valueOf(score);

        BigDecimal divisor = new BigDecimal(100000);
        BigDecimal price = scoreAsBigDecimal.divideToIntegralValue(divisor);


        return new V2MatchOrder(
                stockCode,
                orderType,
                price,
                BigDecimal.valueOf(quantity),
                UUID.fromString(parts[1]),
                UUID.fromString(parts[2]),
                score
        );

    }
    private void saveOrderToRedis(V2MatchOrder order) {
        String key = order.getOrderType().equals(OrderType.BUY) ? "kj_buy_orders:" + order.getTradingPair() : "kj_sell_orders:" + order.getTradingPair();
        ZSetOperations<String, String> zSetOperations = redisTemplate.opsForZSet();
        double score = calcScore(order);
        String strOrder = serializeOrder(order);
        zSetOperations.add(key, strOrder, score);
    }
    private void resaveOrderToRedis(V2MatchOrder order) {
        String key = order.getOrderType().equals(OrderType.BUY) ? "kj_buy_orders:" + order.getTradingPair() : "kj_sell_orders:" + order.getTradingPair();
        ZSetOperations<String, String> zSetOperations = redisTemplate.opsForZSet();
        String strOrder = serializeOrder(order);
        zSetOperations.add(key, strOrder, order.getScore());
    }

    private String serializeOrder(V2MatchOrder order) {
        return order.getQuantity() +"|"+order.getUserId()+"|"+order.getOrderId();
    }

    private double calcScore(V2MatchOrder order) {
        long rawTime = System.currentTimeMillis() % 100000;

        int timePart = (order.getOrderType() == OrderType.BUY) ? (100000 - (int) rawTime) : (int) rawTime;
        String timeStr = String.format("%05d", timePart);

        return Double.parseDouble(timeStr) + order.getPrice().doubleValue() * 100000;
    }

    private void removeOrderFromRedis(V2MatchOrder order) {
        String key = order.getOrderType().equals(OrderType.BUY) ? "kj_buy_orders:" + order.getTradingPair() : "kj_sell_orders:" + order.getTradingPair();
        ZSetOperations<String, String> zSetOperations = redisTemplate.opsForZSet();
        zSetOperations.remove(key, serializeOrder(order));
    }

    private V2MatchOrder updateOrderQuantity(V2MatchOrder order, BigDecimal remainingQuantity) {
        return new V2MatchOrder(
                order.getTradingPair(),
                order.getOrderType(),
                order.getPrice(),
                remainingQuantity,
                order.getUserId(),
                order.getOrderId(),
                order.getScore()
        );
    }
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public static class V2MatchOrder {
        private String tradingPair; // 종목 코드
        private OrderType orderType; // 주문 유형 (매수/매도)
        private BigDecimal price; // 가격
        private BigDecimal quantity; // 수량
        private UUID userId; // 사용자 ID
        private UUID orderId;
        private double score;


        public static V2MatchOrder fromCommand(CreateMatchingCommand command) {
            return new MatchingServiceV2.V2MatchOrder(
                    command.tradingPair(),
                    command.orderType(),
                    command.price(),
                    command.quantity(),
                    command.userId(),
                    command.orderId(),
                    0);
        }
    }

}
