package com.exchange.matching.domain.service;

import com.exchange.matching.application.command.CreateMatchingCommand;
import com.exchange.matching.application.enums.OrderType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


@SpringBootTest
@DisplayName("MatchingServiceV4 통합 테스트")
class MatchingServiceV4IntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(MatchingServiceV4IntegrationTest.class);
    @Autowired
    private MatchingServiceV4 matchingService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private static final String SELL_ORDER_KEY = "v4:orders:sell:";
    private static final String BUY_ORDER_KEY = "v4:orders:buy:";
    private static final String TRADING_PAIR = "BTC/KRW";

    @BeforeEach
    void setUp() {
        // 테스트 시작 전 Redis 데이터 초기화
        redisTemplate.delete(SELL_ORDER_KEY + TRADING_PAIR);
        redisTemplate.delete(BUY_ORDER_KEY + TRADING_PAIR);
    }
//
//    @AfterEach
//    void cleanUp() {
//        // 테스트 종료 후 Redis 데이터 정리
//        redisTemplate.delete(SELL_ORDER_KEY + TRADING_PAIR);
//        redisTemplate.delete(BUY_ORDER_KEY + TRADING_PAIR);
//    }

    @Test
    @DisplayName("미체결 주문 테스트")
    void testUnmatchedOrders() {
        // 매수 주문 생성
        CreateMatchingCommand buyCommand1 = new CreateMatchingCommand(
                TRADING_PAIR,
                OrderType.BUY,
                new BigDecimal("9000"),
                new BigDecimal("0.1"),
                UUID.randomUUID(),
                UUID.randomUUID()
        );

        CreateMatchingCommand buyCommand2 = new CreateMatchingCommand(
                TRADING_PAIR,
                OrderType.BUY,
                new BigDecimal("9000"),
                new BigDecimal("0.3"),
                UUID.randomUUID(),
                UUID.randomUUID()
        );

        CreateMatchingCommand buyCommand3 = new CreateMatchingCommand(
                TRADING_PAIR,
                OrderType.BUY,
                new BigDecimal("8700"),
                new BigDecimal("0.1"),
                UUID.randomUUID(),
                UUID.randomUUID()
        );

        CreateMatchingCommand buyCommand4 = new CreateMatchingCommand(
                TRADING_PAIR,
                OrderType.BUY,
                new BigDecimal("8900"),
                new BigDecimal("0.3"),
                UUID.randomUUID(),
                UUID.randomUUID()
        );

        // 매도 주문 생성
        CreateMatchingCommand sellCommand1 = new CreateMatchingCommand(
                TRADING_PAIR,
                OrderType.SELL,
                new BigDecimal("9500"),
                new BigDecimal("0.3"),
                UUID.randomUUID(),
                UUID.randomUUID()
        );

        CreateMatchingCommand sellCommand2 = new CreateMatchingCommand(
                TRADING_PAIR,
                OrderType.SELL,
                new BigDecimal("9700"),
                new BigDecimal("0.6"),
                UUID.randomUUID(),
                UUID.randomUUID()
        );

        CreateMatchingCommand sellCommand3 = new CreateMatchingCommand(
                TRADING_PAIR,
                OrderType.SELL,
                new BigDecimal("9700"),
                new BigDecimal("0.1"),
                UUID.randomUUID(),
                UUID.randomUUID()
        );

        CreateMatchingCommand sellCommand4 = new CreateMatchingCommand(
                TRADING_PAIR,
                OrderType.SELL,
                new BigDecimal("10000"),
                new BigDecimal("0.2"),
                UUID.randomUUID(),
                UUID.randomUUID()
        );

        CreateMatchingCommand sellCommand5 = new CreateMatchingCommand(
                TRADING_PAIR,
                OrderType.SELL,
                new BigDecimal("11000"),
                new BigDecimal("0.1"),
                UUID.randomUUID(),
                UUID.randomUUID()
        );

        // 모든 주문 실행
        matchingService.matchOrders(buyCommand1);
        matchingService.matchOrders(buyCommand2);
        matchingService.matchOrders(buyCommand3);
        matchingService.matchOrders(buyCommand4);
        matchingService.matchOrders(sellCommand1);
        matchingService.matchOrders(sellCommand2);
        matchingService.matchOrders(sellCommand3);
        matchingService.matchOrders(sellCommand4);
        matchingService.matchOrders(sellCommand5);

        log.info("================미체결 주문 입력 끝================");
    }

    @Test
    @DisplayName("완전 체결 주문 테스트")
    void testCompletedOrders() {
        testUnmatchedOrders();

        CreateMatchingCommand sellCommand = new CreateMatchingCommand(
                TRADING_PAIR,
                OrderType.SELL,
                new BigDecimal("9000"),
                new BigDecimal("0.1"),
                UUID.randomUUID(),
                UUID.randomUUID()
        );

        matchingService.matchOrders(sellCommand);

        CreateMatchingCommand buyCommand = new CreateMatchingCommand(
                TRADING_PAIR,
                OrderType.BUY,
                new BigDecimal("9500"),
                new BigDecimal("0.3"),
                UUID.randomUUID(),
                UUID.randomUUID()
        );

        matchingService.matchOrders(buyCommand);
    }

    @Test
    @DisplayName("부분 체결 주문 테스트 (반대 주문보다 수량이 적을때)")
    void testPartialMatchingWithSmallerQuantity() {
        testUnmatchedOrders();

        CreateMatchingCommand buyCommand = new CreateMatchingCommand(
                TRADING_PAIR,
                OrderType.BUY,
                new BigDecimal("9600"),
                new BigDecimal("0.1"),
                UUID.randomUUID(),
                UUID.randomUUID()
        );

        matchingService.matchOrders(buyCommand);

        CreateMatchingCommand sellCommand = new CreateMatchingCommand(
                TRADING_PAIR,
                OrderType.SELL,
                new BigDecimal("9000"),
                new BigDecimal("0.05"),
                UUID.randomUUID(),
                UUID.randomUUID()
        );

        matchingService.matchOrders(sellCommand);
    }

    @Test
    @DisplayName("완전 체결 주문 테스트 (반대 주문보다 수량이 많을때)\n")
    void testPartialMatchingWithLargerQuantity() {
        testUnmatchedOrders();

        CreateMatchingCommand buyCommand = new CreateMatchingCommand(
                TRADING_PAIR,
                OrderType.BUY,
                new BigDecimal("10000"),
                new BigDecimal("1.1"),
                UUID.randomUUID(),
                UUID.randomUUID()
        );

        matchingService.matchOrders(buyCommand);

        CreateMatchingCommand sellCommand = new CreateMatchingCommand(
                TRADING_PAIR,
                OrderType.SELL,
                new BigDecimal("8600"),
                new BigDecimal("0.9"),
                UUID.randomUUID(),
                UUID.randomUUID()
        );

        matchingService.matchOrders(sellCommand);
    }

    @Test
    @DisplayName("동일 가격 주문의 시간 우선순위 테스트")
    void testTimeBasedPriority() {
        // 1. Redis 초기화
        String buyOrderKey = "v4:orders:buy:" + TRADING_PAIR;
        String sellOrderKey = "v4:orders:sell:" + TRADING_PAIR;
        redisTemplate.delete(buyOrderKey);
        redisTemplate.delete(sellOrderKey);

        // 2. 첫 번째 매수 주문 등록
        UUID user1Id = UUID.randomUUID();
        CreateMatchingCommand firstBuyCommand = new CreateMatchingCommand(
                TRADING_PAIR,
                OrderType.BUY,
                new BigDecimal("10000"),  // 동일 가격
                new BigDecimal("0.4"),
                user1Id,
                UUID.randomUUID()
        );
        matchingService.matchOrders(firstBuyCommand);

        // 3. 시간 간격 (85초)
//        try {
//            Thread.sleep(85000);
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//        }

        // 4. 두 번째 매수 주문 등록 (동일 가격)
        UUID user2Id = UUID.randomUUID();
        CreateMatchingCommand secondBuyCommand = new CreateMatchingCommand(
                TRADING_PAIR,
                OrderType.BUY,
                new BigDecimal("10000"),  // 동일 가격
                new BigDecimal("0.5"),
                user2Id,
                UUID.randomUUID()
        );
        matchingService.matchOrders(secondBuyCommand);

        // 5. Redis에서 주문 정보 확인
        // ZREVRANGE로 첫 번째 주문(오래된 주문)이 우선 처리되는지 확인
        Set<ZSetOperations.TypedTuple<String>> orders = redisTemplate.opsForZSet()
                .reverseRangeWithScores(buyOrderKey, 0, -1);

        // 6. 결과 검증
        assertNotNull(orders);
        assertEquals(2, orders.size());

        // 7. 첫 번째로 매칭될 주문(가장 우선순위 높은 주문) 확인
        String firstOrder = redisTemplate.opsForZSet().reverseRange(buyOrderKey, 0, 0)
                .stream().findFirst().orElse(null);
        assertNotNull(firstOrder);

        // 첫 번째 주문의 타임스탬프와 사용자 ID 추출
//        String[] firstOrderParts = firstOrder.split(":");
//        String timeStampPart = firstOrderParts[0];
//        String orderDetailsPart = firstOrderParts[1];
        String userId = firstOrder.split("\\|")[2];

        // 타임스탬프가 반전되었으므로 값이 큰 경우(작은 원래 시간)가 오래된 주문
        // 첫 번째 사용자의 주문이 우선 매칭되는지 확인
        assertEquals(user1Id.toString(), userId, "오래된 주문(첫 번째 사용자)이 우선 매칭되어야 함");

//        // 8. 매도 주문 등록 (매칭 발생)
//        CreateMatchingCommand sellCommand = new CreateMatchingCommand(
//                TRADING_PAIR,
//                OrderType.SELL,
//                new BigDecimal("10000"),
//                new BigDecimal("0.3"),
//                UUID.randomUUID()
//        );
//        matchingService.matchOrders(sellCommand);
//
//        // 9. 매칭 후 첫 번째 주문 수량이 감소했는지 확인
//        String updatedFirstOrder = redisTemplate.opsForZSet().reverseRange(buyOrderKey, 0, 0)
//                .stream().findFirst().orElse(null);
//        assertNotNull(updatedFirstOrder);
//
//        // 주문 수량 확인
//        String[] updatedParts = updatedFirstOrder.split(":");
//        String updatedOrderDetails = updatedParts[1];
//        BigDecimal remainingQuantity = new BigDecimal(updatedOrderDetails.split("\\|")[0]);
//
//        // 원래 0.5에서 0.3이 매칭되었으므로 0.2가 남아야 함
//        assertEquals(0, remainingQuantity.compareTo(new BigDecimal("0.2")),
//                "첫 번째 주문(오래된 주문)이 우선 매칭되어 수량이 감소해야 함");
    }
}