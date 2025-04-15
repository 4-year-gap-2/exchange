package com.exchange.matching.domain.service;

import com.exchange.matching.application.command.CreateMatchingCommand;
import com.exchange.matching.application.dto.enums.OrderType;
import com.exchange.matching.application.service.MatchingServiceV3;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.UUID;


@SpringBootTest
@DisplayName("MatchingServiceV3 통합 테스트")
class MatchingServiceV3IntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(MatchingServiceV3IntegrationTest.class);
    @Autowired
    private MatchingServiceV3 matchingService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private static final String SELL_ORDER_KEY = "mjy:orders:sell:";
    private static final String BUY_ORDER_KEY = "mjy:orders:buy:";
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
}