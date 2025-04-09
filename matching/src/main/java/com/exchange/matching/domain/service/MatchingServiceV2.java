package com.exchange.matching.domain.service;

import com.exchange.matching.application.command.CreateMatchingCommand;
import com.exchange.matching.application.dto.enums.OrderType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;


import java.math.BigDecimal;


@Service
@RequiredArgsConstructor
@Slf4j
public class MatchingServiceV2 implements MatchingService {


    private final RedisTemplate<String, CreateMatchingCommand> redisTemplate;


    @Override
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
                    orderMatching(matchedOrder, incomingOrder, matchedQuantity , 1);
                    remainingQuantity = remainingQuantity.subtract(matchedQuantity);
                } else {
                    // 미체결 주문 수량 보다 주문한 수량이 적으면
                    // 미체결 주문 수랭 차감 후 저장
                    orderMatching(matchedOrder, incomingOrder, incomingOrder.quantity(),0);
//                    updateOrderQuantity(matchedOrder, matchedQuantity.subtract(remainingQuantity));
                    remainingQuantity = BigDecimal.ZERO;
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

    private void orderMatching(CreateMatchingCommand matchedOrder, CreateMatchingCommand incomingOrder, BigDecimal matchedQuantity, int code) {

        if(code == 1){
            removeOrderFromRedis(matchedOrder);
        }else{
            removeOrderFromRedis(matchedOrder);
            CreateMatchingCommand updateMatchedOrder = updateOrderQuantity(matchedOrder, matchedOrder.quantity().subtract(matchedQuantity));
            saveOrderToRedis(updateMatchedOrder);
        }

        log.info("체결완료");
    }


    private void saveOrderToRedis(CreateMatchingCommand order) {
        String key = order.orderType().equals(OrderType.BUY) ? "kj_buy_orders:" + order.tradingPair() : "kj_sell_orders:" + order.tradingPair();
        ZSetOperations<String, CreateMatchingCommand> zSetOperations = redisTemplate.opsForZSet();
        zSetOperations.add(key, order, order.price().doubleValue());
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
