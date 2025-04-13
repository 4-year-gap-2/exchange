package com.exchange.matching.domain.service;

import com.exchange.matching.application.command.CreateMatchingCommand;
import com.exchange.matching.application.dto.enums.OrderType;
import com.exchange.matching.infrastructure.dto.KafkaMatchingEvent;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;


import java.math.BigDecimal;


@Service
@RequiredArgsConstructor
@Slf4j
public class MatchingServiceV2 implements MatchingService {

    private final RedisTemplate<String, CreateMatchingCommand> redisTemplate;

    @Override
    @Transactional
    public void matchOrders(CreateMatchingCommand command) {
        // 카프카에서 값 읽기 토픽은 [4yearGap.order.orderEvent.match]
        matchingProcess(command);
    }

    private void matchingProcess(CreateMatchingCommand incomingOrder) {
        BigDecimal remainingQuantity = incomingOrder.quantity();
        while (remainingQuantity.compareTo(BigDecimal.ZERO) != 0) {
            CreateMatchingCommand matchedOrder = findMatchingOrder(incomingOrder.tradingPair(), incomingOrder.orderType());
            if (matchedOrder == null) {
                saveOrderToRedis(updateOrderQuantity(incomingOrder, remainingQuantity));
                return;
            }

            BigDecimal matchedQuantity = matchedOrder.quantity();
            // 현재 거래 가능한 미체결 주문이 있다
            if (incomingOrder.orderType().equals(OrderType.SELL) && matchedOrder.price().compareTo(incomingOrder.price()) >= 0 ||
                    incomingOrder.orderType().equals(OrderType.BUY) && matchedOrder.price().compareTo(incomingOrder.price()) <= 0) {
                // 미체결 주문 수량 보다 주문한 수량이 많으면
                // 미체결 주문 삭제
                if (remainingQuantity.compareTo(matchedQuantity) >= 0) {
                    removeOrderFromRedis(matchedOrder);
                    remainingQuantity = remainingQuantity.subtract(matchedQuantity);
                    log.info("체결 남은 주분 수량 {} 완전 체결 수량 {}",remainingQuantity,matchedOrder.quantity());
                } else {
                    // 미체결 주문 수량 보다 주문한 수량이 적으면
                    // 미체결 주문 수랭 차감 후 저장
                    removeOrderFromRedis(matchedOrder);
                    CreateMatchingCommand updateMatchedOrder = updateOrderQuantity(matchedOrder, matchedOrder.quantity().subtract(remainingQuantity));
                    saveOrderToRedis(updateMatchedOrder);
                    remainingQuantity = BigDecimal.ZERO;
                    log.info("체결 남은 주분 수량 {} 완전 체결 수량 {}",remainingQuantity,matchedOrder.quantity());
                }
            } else {
                saveOrderToRedis(updateOrderQuantity(incomingOrder, remainingQuantity));
                return;
            }
        }
    }

    private CreateMatchingCommand findMatchingOrder(String stockCode, OrderType orderType) {
        String key = orderType.equals(OrderType.SELL) ? "kj_buy_orders:" + stockCode : "kj_sell_orders:" + stockCode;
        ZSetOperations<String, CreateMatchingCommand> zSetOperations = redisTemplate.opsForZSet();
        if (orderType.equals(OrderType.SELL)) {
            return zSetOperations.reverseRange(key, 0, 0).stream().findFirst().orElse(null);
        } else {
            return zSetOperations.range(key, 0, 0).stream().findFirst().orElse(null);
        }
    }


    private void saveOrderToRedis(CreateMatchingCommand order) {
        String key = order.orderType().equals(OrderType.BUY) ? "kj_buy_orders:" + order.tradingPair() : "kj_sell_orders:" + order.tradingPair();
        ZSetOperations<String, CreateMatchingCommand> zSetOperations = redisTemplate.opsForZSet();

        double score = calcScore(order);

        zSetOperations.add(key, order, score);
    }

    private double calcScore(CreateMatchingCommand order) {
        long rawTime = System.currentTimeMillis() % 100000;

        int timePart = (order.orderType() == OrderType.BUY) ? (100000 - (int) rawTime) : (int) rawTime ;
        String timeStr = "0." + String.format("%05d", timePart);

        double score = Double.parseDouble(timeStr) + order.price().doubleValue();
        return score;
    }

    private void removeOrderFromRedis(CreateMatchingCommand order) {
        String key = order.orderType().equals(OrderType.BUY) ? "kj_buy_orders:" + order.tradingPair() : "kj_sell_orders:" + order.tradingPair();
        ZSetOperations<String, CreateMatchingCommand> zSetOperations = redisTemplate.opsForZSet();
        zSetOperations.remove(key, order);
    }

    private CreateMatchingCommand updateOrderQuantity(CreateMatchingCommand order, BigDecimal remainingQuantity) {
        return new CreateMatchingCommand(
                order.tradingPair(),
                order.orderType(),
                order.price(),
                remainingQuantity,
                order.userId()
        );
    }
}
