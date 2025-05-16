package com.exchange.order_completed.application.service;

import com.exchange.order_completed.application.command.CreateMatchedOrderStoreCommand;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@Slf4j
public class CurrentPriceService {

    private final RedisTemplate<String, String> redisTemplate;

    // Redis 키 접두사 정의
    private static final String CURRENT_PRICE_KEY_PREFIX = "market:current_price:";

    @Autowired
    public CurrentPriceService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 체결 이벤트에서 현재 가격 업데이트
     */
    public void updateCurrentPrice(List<CreateMatchedOrderStoreCommand> matchedOrders) {
        for (CreateMatchedOrderStoreCommand order : matchedOrders) {
            String tradingPair = order.tradingPair();
            BigDecimal executionPrice = order.price();

            // 현재 가격 저장
            redisTemplate.opsForValue().set(CURRENT_PRICE_KEY_PREFIX + tradingPair, executionPrice.toString());
            log.debug("{}의 현재 가격 업데이트: {}", tradingPair, executionPrice);
        }
    }

    /**
     * 현재 가격 조회
     */
    public BigDecimal getCurrentPrice(String tradingPair) {
        String priceStr = redisTemplate.opsForValue().get(CURRENT_PRICE_KEY_PREFIX + tradingPair);

        if (priceStr != null) {
            try {
                return new BigDecimal(priceStr);
            } catch (NumberFormatException e) {
                log.error("현재 가격 형식 오류: {}", priceStr);
            }
        }

        return null;
    }
}
