package com.exchange.matching.domain.service;

import com.exchange.matching.application.command.CreateMatchingCommand;
import com.exchange.matching.application.dto.enums.OrderType;
import com.exchange.matching.infrastructure.dto.KafkaMatchingEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;


import java.math.BigDecimal;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Slf4j
public class MatchingServiceV2 implements MatchingService {


    private final RedisTemplate<String, CreateMatchingCommand> redisTemplate;


    @Override
    public void matchOrders(KafkaMatchingEvent event) {

        // 카프카에서 값 읽기 토픽은 [4yearGap.order.orderEvent.match]
        CreateMatchingCommand command = new CreateMatchingCommand(
                "BTC/KRW",
                OrderType.SELL,
                BigDecimal.valueOf(97000),
                BigDecimal.valueOf(2.3),
                UUID.randomUUID() // UUID를 String으로 변환
        );

        matchingProcess(command);
    }

    public CreateMatchingCommand getOrder(CreateMatchingCommand command) {
        CreateMatchingCommand tempRedisValue = null;
        if (command.orderType().equals(OrderType.SELL)) {
            tempRedisValue = getHighestBuyOrderForSell(command.tradingPair());
        }
        if (command.orderType().equals(OrderType.BUY)) {
            tempRedisValue = getLowestSellOrderForBuy(command.tradingPair());
        }
        return tempRedisValue;
    }

    private void matchingProcess(CreateMatchingCommand incomingOrder) {
        BigDecimal remainingQuantity = incomingOrder.quantity();
        while (remainingQuantity.compareTo(BigDecimal.ZERO) > 0) {
            CreateMatchingCommand matchedOrder = findMatchingOrder(incomingOrder.tradingPair(), incomingOrder.orderType());
            if (matchedOrder == null) {
                saveOrderToRedis(updateOrderQuantity(incomingOrder, remainingQuantity));
                return;
            }

            BigDecimal matchedQuantity = matchedOrder.quantity();
            if (incomingOrder.orderType().equals(OrderType.SELL) && matchedOrder.price().compareTo(incomingOrder.price()) >= 0 ||
                    incomingOrder.orderType().equals(OrderType.BUY) && matchedOrder.price().compareTo(incomingOrder.price()) <= 0) {
                if (remainingQuantity.compareTo(matchedQuantity) >= 0) {
                    orderMatching(matchedOrder, incomingOrder, matchedQuantity);
                    removeOrderFromRedis(matchedOrder);
                    remainingQuantity = remainingQuantity.subtract(matchedQuantity);
                } else {
                    orderMatching(matchedOrder, incomingOrder, remainingQuantity);
                    updateOrderQuantity(matchedOrder, matchedQuantity.subtract(remainingQuantity));
                    remainingQuantity = BigDecimal.ZERO;
                }
            } else {
                saveOrderToRedis(updateOrderQuantity(incomingOrder, remainingQuantity));
                return;
            }
        }
    }

    private CreateMatchingCommand findMatchingOrder(String stockCode, OrderType orderType) {
        String key = orderType.equals(OrderType.SELL) ? "buy_orders:" + stockCode : "sell_orders:" + stockCode;
        ZSetOperations<String, CreateMatchingCommand> zSetOperations = redisTemplate.opsForZSet();
        if (orderType.equals(OrderType.SELL)) {
            return zSetOperations.reverseRange(key, 0, 0).stream().findFirst().orElse(null);
        } else {
            return zSetOperations.range(key, 0, 0).stream().findFirst().orElse(null);
        }
    }

    private void orderMatching(CreateMatchingCommand matchedOrder, CreateMatchingCommand incomingOrder, BigDecimal matchedQuantity) {
        incomingOrder.quantity().subtract(matchedQuantity);
        saveOrderToRedis(incomingOrder);
    }

    private void saveOrderToRedis(CreateMatchingCommand order) {
        String key = order.orderType().equals(OrderType.BUY) ? "buy_orders:" + order.tradingPair() : "sell_orders:" + order.tradingPair();
        ZSetOperations<String, CreateMatchingCommand> zSetOperations = redisTemplate.opsForZSet();
        zSetOperations.add(key, order, order.price().doubleValue());
    }

    private void removeOrderFromRedis(CreateMatchingCommand order) {
        String key = order.orderType().equals(OrderType.BUY) ? "buy_orders:" + order.tradingPair() : "sell_orders:" + order.tradingPair();
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

    public boolean isDecimalOnly(BigDecimal value) {
        return Pattern.matches("^0?\\.\\d+$", value.toPlainString());
    }

    private CreateMatchingCommand getHighestBuyOrderForSell(String stockCode) {
        String key = "buy_orders:" + stockCode;
        ZSetOperations<String, CreateMatchingCommand> zSetOperations = redisTemplate.opsForZSet();
        return zSetOperations.reverseRange(key, 0, 0).stream().findFirst().orElse(null);
    }

    private CreateMatchingCommand getLowestSellOrderForBuy(String stockCode) {
        String key = "sell_orders:" + stockCode;
        ZSetOperations<String, CreateMatchingCommand> zSetOperations = redisTemplate.opsForZSet();
        return zSetOperations.range(key, 0, 0).stream().findFirst().orElse(null);
    }
}
