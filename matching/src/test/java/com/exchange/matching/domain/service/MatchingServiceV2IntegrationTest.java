package com.exchange.matching.domain.service;


import com.exchange.matching.application.command.CreateMatchingCommand;
import com.exchange.matching.application.dto.enums.OrderType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@DisplayName("MatchingServiceV2 통합 테스트")
public class  MatchingServiceV2IntegrationTest {


    @Autowired
    private MatchingServiceV2 matchingService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        // DEL kj_buy_orders:BTC/KRW 명령어 실행
        redisTemplate.delete("kj_buy_orders:BTC/KRW");
        redisTemplate.delete("kj_sell_orders:BTC/KRW");
    }

    @Test
    @DisplayName("매수 주문 처리 - 매칭되는 매도 주문 없음")
    void buyOrderWithNoMatchingSellOrder() {

        CreateMatchingCommand command1 = new CreateMatchingCommand(
                "BTC/KRW",
                OrderType.BUY,
                BigDecimal.valueOf(9000),
                BigDecimal.valueOf(0.1),
                userId,
                UUID.randomUUID()
        );
        CreateMatchingCommand command2 = new CreateMatchingCommand(
                "BTC/KRW",
                OrderType.BUY,
                BigDecimal.valueOf(9000),
                BigDecimal.valueOf(0.3),
                userId,
                UUID.randomUUID()
        );
        CreateMatchingCommand command3 = new CreateMatchingCommand(
                "BTC/KRW",
                OrderType.BUY,
                BigDecimal.valueOf(8700),
                BigDecimal.valueOf(0.1),
                userId,
                UUID.randomUUID()
        );
        CreateMatchingCommand command4 = new CreateMatchingCommand(
                "BTC/KRW",
                OrderType.BUY,
                BigDecimal.valueOf(8900),
                BigDecimal.valueOf(0.3),
                userId,
                UUID.randomUUID()
        );
        CreateMatchingCommand command5 = new CreateMatchingCommand(
                "BTC/KRW",
                OrderType.SELL,
                BigDecimal.valueOf(9500),
                BigDecimal.valueOf(0.3),
                userId,
                UUID.randomUUID()
        );
        CreateMatchingCommand command6 = new CreateMatchingCommand(
                "BTC/KRW",
                OrderType.SELL,
                BigDecimal.valueOf(9700),
                BigDecimal.valueOf(0.6),
                userId,
                UUID.randomUUID()
        );
        CreateMatchingCommand command7 = new CreateMatchingCommand(
                "BTC/KRW",
                OrderType.SELL,
                BigDecimal.valueOf(10000),
                BigDecimal.valueOf(0.2),
                userId,
                UUID.randomUUID()
        );
        CreateMatchingCommand command8 = new CreateMatchingCommand(
                "BTC/KRW",
                OrderType.SELL,
                BigDecimal.valueOf(11000),
                BigDecimal.valueOf(0.1),
                userId,
                UUID.randomUUID()
        );
        CreateMatchingCommand command9 = new CreateMatchingCommand(
                "BTC/KRW",
                OrderType.SELL,
                BigDecimal.valueOf(9700),
                BigDecimal.valueOf(0.1),
                userId,
                UUID.randomUUID()
        );

        matchingService.matchOrders(command1);
        matchingService.matchOrders(command2);
        matchingService.matchOrders(command3);
        matchingService.matchOrders(command4);
        matchingService.matchOrders(command5);
        matchingService.matchOrders(command6);
        matchingService.matchOrders(command7);
        matchingService.matchOrders(command8);
        matchingService.matchOrders(command9);


        Set<String> buyOrders = redisTemplate.opsForZSet().range("kj_buy_orders:BTC/KRW" , 0, -1);
        assertNotNull(buyOrders);
        assertEquals(3, buyOrders.size());

        Set<String> sellOrders = redisTemplate.opsForZSet().range("kj_sell_orders:BTC/KRW" , 0, -1);
        assertNotNull(sellOrders);
        assertEquals(4, sellOrders.size());
    }

    @Test
    @DisplayName("완전 체결")
    void perfectMatch() throws InterruptedException {
        CreateMatchingCommand firstBuyOrder = new CreateMatchingCommand(
                "BTC/KRW",
                OrderType.BUY,
                BigDecimal.valueOf(9000),
                BigDecimal.valueOf(0.1),
                userId,
                UUID.randomUUID()
        );
        CreateMatchingCommand secondBuyOrder = new CreateMatchingCommand(
                "BTC/KRW",
                OrderType.BUY,
                BigDecimal.valueOf(9000),
                BigDecimal.valueOf(0.1),
                userId,
                UUID.randomUUID()
        );
        CreateMatchingCommand sellOrder = new CreateMatchingCommand(
                "BTC/KRW",
                OrderType.SELL,
                BigDecimal.valueOf(9000),
                BigDecimal.valueOf(0.1),
                userId,
                UUID.randomUUID()
        );

        matchingService.matchOrders(firstBuyOrder);
        Thread.sleep(2000);
        matchingService.matchOrders(secondBuyOrder);
        matchingService.matchOrders(sellOrder);

        //첫번째 주문은 완전 체결이 되고 주문이 하나만 남아야 함
        String remainingOrder = redisTemplate.opsForZSet().reverseRange("kj_buy_orders:BTC/KRW", 0, 0).stream().findFirst().orElse(null);
        assertNotNull(remainingOrder);
        assertEquals(remainingOrder,"0.1|" + userId.toString()+"|" ,secondBuyOrder.orderId().toString());
        System.out.println(remainingOrder);
    }

    @Test
    @DisplayName("부분 체결")
    void notPerfectMatch() throws InterruptedException {
        CreateMatchingCommand buyOrder = new CreateMatchingCommand(
                "BTC/KRW",
                OrderType.BUY,
                BigDecimal.valueOf(9600),
                BigDecimal.valueOf(0.2),
                userId,
                UUID.randomUUID()
        );
        CreateMatchingCommand sellOrder = new CreateMatchingCommand(
                "BTC/KRW",
                OrderType.SELL,
                BigDecimal.valueOf(9500),
                BigDecimal.valueOf(0.1),
                userId,
                UUID.randomUUID()
        );

        matchingService.matchOrders(buyOrder);
        matchingService.matchOrders(sellOrder);

        //첫번째 주문은 완전 체결이 되고 주문이 하나만 남아야 함
        String remainingOrder = redisTemplate.opsForZSet().reverseRange("kj_buy_orders:BTC/KRW", 0, 0).stream().findFirst().orElse(null);
        assertNotNull(remainingOrder);

        assertEquals(remainingOrder,"0.1|" + userId.toString()+"|" ,buyOrder.orderId().toString());
        System.out.println(remainingOrder);
    }

    @Test
    @DisplayName("부분 체결 반대 상황")
    void notPerfectMatchOpp() throws InterruptedException {
        CreateMatchingCommand buyOrder = new CreateMatchingCommand(
                "BTC/KRW",
                OrderType.BUY,
                BigDecimal.valueOf(9600),
                BigDecimal.valueOf(0.1),
                userId,
                UUID.randomUUID()
        );
        CreateMatchingCommand sellOrder = new CreateMatchingCommand(
                "BTC/KRW",
                OrderType.SELL,
                BigDecimal.valueOf(9500),
                BigDecimal.valueOf(0.2),
                userId,
                UUID.randomUUID()
        );

        matchingService.matchOrders(buyOrder);
        matchingService.matchOrders(sellOrder);

        //첫번째 주문은 완전 체결이 되고 주문이 하나만 남아야 함
        String remainingOrder = redisTemplate.opsForZSet().range("kj_sell_orders:BTC/KRW", 0, 0).stream().findFirst().orElse(null);
        assertNotNull(remainingOrder);
        assertEquals(remainingOrder,"0.1|" + userId.toString()+"|" ,sellOrder.orderId().toString());

        System.out.println(remainingOrder);
    }

    @Test
    @DisplayName("오래된 주문 조회")
    void getLaterOrder() throws InterruptedException {

        CreateMatchingCommand buyOrder = new CreateMatchingCommand(
                "BTC/KRW",
                OrderType.BUY,
                BigDecimal.valueOf(9500),
                BigDecimal.valueOf(0.1),
                userId,
                UUID.randomUUID()
        );
        CreateMatchingCommand buyOrder2 = new CreateMatchingCommand(
                "BTC/KRW",
                OrderType.BUY,
                BigDecimal.valueOf(9500),
                BigDecimal.valueOf(0.2),
                userId,
                UUID.randomUUID()
        );

        matchingService.matchOrders(buyOrder);
        Thread.sleep(2000);
        matchingService.matchOrders(buyOrder2);

        String remainingOrder = redisTemplate.opsForZSet().reverseRange("kj_buy_orders:BTC/KRW", 0, 0).stream().findFirst().orElse(null);

        assertNotNull(remainingOrder);

        assertEquals(remainingOrder,"0.1|" + userId.toString()+"|" ,buyOrder.orderId().toString());
        System.out.println(remainingOrder);

    }
}
