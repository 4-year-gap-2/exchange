package com.exchange.matching.domain.service;

import com.exchange.matching.application.dto.CreateMatchingCommand;
import com.exchange.matching.application.dto.enums.OrderType;
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
    public void matchOrders() {

        // 카프카에서 값 읽기 토픽은 [4yearGap.order.orderEvent.match]
        CreateMatchingCommand command = new CreateMatchingCommand(
                "BTC/KRW",
                OrderType.SELL,
                BigDecimal.valueOf(97000),
                BigDecimal.valueOf(2.3),
                UUID.randomUUID() // UUID를 String으로 변환
        );

        // 레디스에서 값 가지고 오기
        CreateMatchingCommand tempRedisValue = null;
        if (command.orderType().equals(OrderType.SELL)) {
            tempRedisValue = getHighestBuyOrderForSell(command.tradingPair());
        }
        if (command.orderType().equals(OrderType.BUY)) {
            tempRedisValue = getLowestSellOrderForBuy(command.tradingPair());
        }
        matchingProcess(tempRedisValue, command);
    }

    private void matchingProcess(CreateMatchingCommand matchedOrder, CreateMatchingCommand incomingOrder) {
        while (isDecimalOnly(incomingOrder.quantity())){
            if (matchedOrder == null) {

                // 매칭 주문이 없을 경우 그냥 미체결데이터로 저장

                return;
            }

            // 매칭 로직 구현
            // 매도 주문이면서 매도가 가능한 상황
            if (incomingOrder.orderType().equals(OrderType.SELL) && matchedOrder.price().compareTo(incomingOrder.price()) <= 0) {
                // 매도 주문과 매수 주문의 가격 비교 및 체결 로직
                // 주문 수량만큼 감소 하고 다시 저장 또는 미체결 주문 삭제 후 다시 조회 및 체결


            } else if (incomingOrder.orderType().equals(OrderType.BUY) && matchedOrder.price().compareTo(incomingOrder.price()) >= 0) {
                // 매수 주문과 매도 주문의 가격 비교 및 체결 로직
            } else {
                log.info("이런 경우가 있나?");
            }
        }
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
