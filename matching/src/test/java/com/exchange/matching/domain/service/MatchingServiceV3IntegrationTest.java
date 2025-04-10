package com.exchange.matching.domain.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.exchange.matching.application.command.CreateMatchingCommand;
import com.exchange.matching.application.dto.enums.OrderType;
import com.exchange.matching.infrastructure.dto.KafkaMatchingEvent;
import org.apache.kafka.common.protocol.types.Field;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.TestPropertySource;
import scala.util.Random;

@SpringBootTest
//@SpringBootTest(properties = "spring.aop.auto=true")
@DisplayName("MatchingServiceV3 통합 테스트")
class MatchingServiceV3IntegrationTest {

    @Autowired
    private MatchingServiceV3 matchingService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private static final String SELL_ORDER_KEY = "orders:sell:";
    private static final String BUY_ORDER_KEY = "orders:buy:";
    private static final String TRADING_PAIR = "BTC/KRW";

//    @BeforeEach
//    void setUp() {
//        // 테스트 시작 전 Redis 데이터 초기화
//        redisTemplate.delete(SELL_ORDER_KEY + TRADING_PAIR);
//        redisTemplate.delete(BUY_ORDER_KEY + TRADING_PAIR);
//    }
//
//    @AfterEach
//    void cleanUp() {
//        // 테스트 종료 후 Redis 데이터 정리
//        redisTemplate.delete(SELL_ORDER_KEY + TRADING_PAIR);
//        redisTemplate.delete(BUY_ORDER_KEY + TRADING_PAIR);
//    }

    @Test
    @DisplayName("매수 주문 처리 - 매칭되는 매도 주문 없음")
    void buyOrderWithNoMatchingSellOrder() {
        // Given
        UUID userId = UUID.randomUUID();

        CreateMatchingCommand command = new CreateMatchingCommand(
                TRADING_PAIR,
                OrderType.BUY,
                BigDecimal.valueOf(50000),
                BigDecimal.valueOf(1.5),
                userId
        );

        // When
        matchingService.matchOrders(command);

        // Then
        Set<String> buyOrders = redisTemplate.opsForZSet().range(BUY_ORDER_KEY + TRADING_PAIR, 0, -1);
        assertNotNull(buyOrders);
        assertEquals(1, buyOrders.size());

        String buyOrder = buyOrders.iterator().next();
        assertTrue(buyOrder.contains(String.valueOf(1.5))); // 수량 확인
        assertTrue(buyOrder.contains(userId.toString())); // 사용자 ID 확인
    }

    @Test
    @DisplayName("매수/매도 주문 체결 테스트")
    void orderMatchingTest() {
        // Given
        UUID buyUserId = UUID.randomUUID();
        UUID sellUserId = UUID.randomUUID();

        CreateMatchingCommand buyCommand = new CreateMatchingCommand(
                TRADING_PAIR,
                OrderType.BUY,
                BigDecimal.valueOf(50000),
                BigDecimal.valueOf(1.5),
                buyUserId
        );

        CreateMatchingCommand sellCommand = new CreateMatchingCommand(
                TRADING_PAIR,
                OrderType.SELL,
                BigDecimal.valueOf(49000), // 매수가보다 낮은 가격
                BigDecimal.valueOf(1.0),   // 매수량보다 적은 수량
                sellUserId
        );

        // When
        // 먼저 매수 주문 처리
        matchingService.matchOrders(buyCommand);
        // 이제 매도 주문 처리
        matchingService.matchOrders(sellCommand);

    }

    @Test
    @DisplayName("여러 매수/매도 주문 연속 처리 테스트")
    void multipleOrdersProcessingTest() {
        // Given
        // 여러 매수 주문 생성
        CreateMatchingCommand buyCommand1 = new CreateMatchingCommand(
                TRADING_PAIR,
                OrderType.BUY,
                BigDecimal.valueOf(50000),
                BigDecimal.valueOf(1.0),
                UUID.randomUUID()
        );

        CreateMatchingCommand buyCommand2 = new CreateMatchingCommand(
                TRADING_PAIR,
                OrderType.BUY,
                BigDecimal.valueOf(51000),
                BigDecimal.valueOf(2.0),
                UUID.randomUUID()
        );

        CreateMatchingCommand sellCommand1 = new CreateMatchingCommand(
                TRADING_PAIR,
                OrderType.SELL,
                BigDecimal.valueOf(49000), // 모든 매수가보다 낮은 가격
                BigDecimal.valueOf(3.5),   // 모든 매수량의 합보다 큰 수량
                UUID.randomUUID()
        );

        // When
        // 매수 주문들 처리
        matchingService.matchOrders(buyCommand1);
        matchingService.matchOrders(buyCommand2);

        // 이제 매도 주문 처리
        matchingService.matchOrders(sellCommand1);
    }

    @Test
    @DisplayName("동시성 테스트 - 여러 주문 동시 처리")
    void concurrentOrderProcessingTest() throws InterruptedException {
        // Given
        int orderCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(orderCount);
        CountDownLatch latch = new CountDownLatch(orderCount);

        // 5개의 매수 주문과 5개의 매도 주문 생성
        for (int i = 0; i < orderCount / 2; i++) {
            // 매수 주문

            CreateMatchingCommand buyCommand = new CreateMatchingCommand(
                    TRADING_PAIR,
                    OrderType.BUY,
                    BigDecimal.valueOf(50000 + i * 100), // 조금씩 다른 가격
                    BigDecimal.valueOf(1.0),
                    UUID.randomUUID()
            );

            CreateMatchingCommand sellCommand = new CreateMatchingCommand(
                    TRADING_PAIR,
                    OrderType.SELL,
                    BigDecimal.valueOf(49900 - i * 100), // 조금씩 다른 가격
                    BigDecimal.valueOf(1.0),
                    UUID.randomUUID()
            );

            // 매수 주문 처리 작업 제출
            executorService.submit(() -> {
                try {
                    matchingService.matchOrders(buyCommand);
                } finally {
                    latch.countDown();
                }
            });

            // 매도 주문 처리 작업 제출
            executorService.submit(() -> {
                try {
                    matchingService.matchOrders(sellCommand);
                } finally {
                    latch.countDown();
                }
            });
        }

        // 모든 작업이 완료될 때까지 대기
        latch.await(10, TimeUnit.SECONDS);
        executorService.shutdown();
    }
}