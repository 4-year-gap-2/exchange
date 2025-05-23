package com.exchange.matching.domain.service;

import com.exchange.matching.application.command.CreateMatchingCommand;
import com.exchange.matching.application.enums.OrderType;
import com.exchange.matching.domain.entity.UnmatchedOrderA;
import com.exchange.matching.domain.entity.MatchedOrder;
import com.exchange.matching.domain.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
@Rollback(value = false)
public class MatchingServiceV1AIntegrationTest {

    @Autowired
    private MatchingServiceV1A orderMatchingService;

    @Autowired
    private ActivatedOrderReader activatedOrderReader;

    @Autowired
    private ActivatedOrderStore activatedOrderStore;

    @Autowired
    private CompletedOrderReader completedOrderReader;

    @Autowired
    private CompletedOrderStore completedOrderStore;

    private static final String TRADING_PAIR = "BTC/KRW";

    @BeforeEach
    public void setUp(TestInfo testInfo) {
        // 테스트 전에 거래 내역 테이블 초기화
        activatedOrderStore.deleteAll();
        completedOrderStore.deleteAll();

        if (testInfo.getTestMethod().isPresent() && testInfo.getTestMethod().get().isAnnotationPresent(SkipSetUp.class)) {
            return;
        }

        // 테스트 데이터 초기화
        // 매수 주문 (BUY)
        UnmatchedOrderA buyOrder1 = ActivatedOrderFactory.createBuyOrder1();
        UnmatchedOrderA buyOrder2 = ActivatedOrderFactory.createBuyOrder2();
        UnmatchedOrderA buyOrder3 = ActivatedOrderFactory.createBuyOrder3();
        UnmatchedOrderA buyOrder4 = ActivatedOrderFactory.createBuyOrder4();
        // 매도 주문 (SELL)
        UnmatchedOrderA sellOrder1 = ActivatedOrderFactory.createSellOrder1();
        UnmatchedOrderA sellOrder2 = ActivatedOrderFactory.createSellOrder2();
        UnmatchedOrderA sellOrder3 = ActivatedOrderFactory.createSellOrder3();
        UnmatchedOrderA sellOrder4 = ActivatedOrderFactory.createSellOrder4();
        UnmatchedOrderA sellOrder5 = ActivatedOrderFactory.createSellOrder5();

        List<UnmatchedOrderA> unmatchedOrderAList = List.of(buyOrder1, buyOrder2, buyOrder3, buyOrder4,
                sellOrder1, sellOrder2, sellOrder3, sellOrder4, sellOrder5);

        activatedOrderStore.saveAll(unmatchedOrderAList);
    }

    @Test
    @SkipSetUp
    @DisplayName("주문 매칭이 실패하여 모든 주문이 미체결 상태로 남아야 한다.")
    public void testOrdersRemainPendingWhenNoMatchingOccurs() {
        // 매수 주문 (BUY)
        CreateMatchingCommand buyOrder1 = MatchingCommandFactory.createBuyOrder1();
        CreateMatchingCommand buyOrder2 = MatchingCommandFactory.createBuyOrder2();
        CreateMatchingCommand buyOrder3 = MatchingCommandFactory.createBuyOrder3();
        CreateMatchingCommand buyOrder4 = MatchingCommandFactory.createBuyOrder4();

        // 매도 주문 (SELL)
        CreateMatchingCommand sellOrder1 = MatchingCommandFactory.createSellOrder1();
        CreateMatchingCommand sellOrder2 = MatchingCommandFactory.createSellOrder2();
        CreateMatchingCommand sellOrder3 = MatchingCommandFactory.createSellOrder3();
        CreateMatchingCommand sellOrder4 = MatchingCommandFactory.createSellOrder4();
        CreateMatchingCommand sellOrder5 = MatchingCommandFactory.createSellOrder5();

        // 매수 주문의 가격은 모두 9000원 이하이고,
        // 매도 주문의 가격은 모두 9500원 이상이므로,
        // 가격 조건이 맞지 않아 어떤 주문도 체결되지 않고 미체결 상태로 거래 내역에 저장
        orderMatchingService.matchOrders(buyOrder1);
        orderMatchingService.matchOrders(buyOrder2);
        orderMatchingService.matchOrders(buyOrder3);
        orderMatchingService.matchOrders(buyOrder4);

        orderMatchingService.matchOrders(sellOrder1);
        orderMatchingService.matchOrders(sellOrder2);
        orderMatchingService.matchOrders(sellOrder3);
        orderMatchingService.matchOrders(sellOrder4);
        orderMatchingService.matchOrders(sellOrder5);

        // 미체결 주문 조회
        List<UnmatchedOrderA> unmatchedOrderAList = activatedOrderReader.findAll();

        // 체결 주문 조회
        List<MatchedOrder> matchedOrderList = completedOrderReader.findAll();

        assertEquals(9, unmatchedOrderAList.size(), "미체결 거래 내역 수는 9건이어야 한다.");
        assertEquals(0, matchedOrderList.size(), "체결 거래 내역 수는 0건이어야 한다.");
    }

    @Test
    @DisplayName("매도 주문과 매수 주문의 가격과 수량이 모두 일치하여 양쪽 주문 모두 완전 체결되어야 한다.")
    public void testOrdersFullyMatched() {
        // 매도 주문 (SELL)
        CreateMatchingCommand sellOrder = new CreateMatchingCommand(
                TRADING_PAIR,
                OrderType.BUY,
                BigDecimal.valueOf(9000),   // Price
                BigDecimal.valueOf(0.1),    // Quantity
                UUID.randomUUID(),  // User ID
                UUID.randomUUID()   // Order ID
        );

        orderMatchingService.matchOrders(sellOrder);

        // 미체결 주문 조회
        List<UnmatchedOrderA> unmatchedOrderABList = activatedOrderReader.findAll();

        // 체결 주문 조회
        List<MatchedOrder> matchedOrderList = completedOrderReader.findAll();

        assertEquals(8, unmatchedOrderABList.size(), "미체결 거래 내역 수는 8건이어야 한다.");
        assertEquals(2, matchedOrderList.size(), "체결 거래 내역 수는 2건이어야 한다.");
    }

    @Test
    @DisplayName("요청 주문(Buy) 수량이 반대 주문(Sell) 수량보다 적을 경우, 요청 주문(Buy)은 완전 체결되고 반대 주문(Sell)은 미체결 상태로 남아야 한다.")
    public void shouldPartiallyMatchSellOrder() {
        // 매수 주문 (BUY)
        CreateMatchingCommand buyOrder = new CreateMatchingCommand(
                TRADING_PAIR,
                OrderType.BUY,
                BigDecimal.valueOf(9600),   // Price
                BigDecimal.valueOf(0.1),    // Quantity
                UUID.randomUUID(),  // User ID
                UUID.randomUUID()   // Order ID
        );

        orderMatchingService.matchOrders(buyOrder);

        // 미체결 주문 조회
        List<UnmatchedOrderA> unmatchedOrderAList = activatedOrderReader.findAll();

        // 체결 주문 조회
        List<MatchedOrder> matchedOrderList = completedOrderReader.findAll();

        assertEquals(9, unmatchedOrderAList.size(), "미체결 거래 내역 수는 9건이어야 한다.");
        assertEquals(2, matchedOrderList.size(), "체결 거래 내역 수는 2건이어야 한다.");
    }

    @Test
    @DisplayName("요청 주문(Sell) 수량이 반대 주문(Buy) 수량보다 적을 경우, 요청 주문(Sell)은 완전 체결되고 반대 주문(Buy)은 미체결 상태로 남아야 한다.")
    public void shouldPartiallyMatchBuyOrder() {
        // 매도 주문 (SELL)
        CreateMatchingCommand sellOrder = new CreateMatchingCommand(
                TRADING_PAIR,
                OrderType.SELL,
                BigDecimal.valueOf(8800),   // Price
                BigDecimal.valueOf(0.05),    // Quantity
                UUID.randomUUID(),  // User ID
                UUID.randomUUID()   // Order ID
        );

        orderMatchingService.matchOrders(sellOrder);

        // 미체결 주문 조회
        List<UnmatchedOrderA> unmatchedOrderAList = activatedOrderReader.findAll();

        // 체결 주문 조회
        List<MatchedOrder> matchedOrderList = completedOrderReader.findAll();

        assertEquals(9, unmatchedOrderAList.size(), "미체결 거래 내역 수는 9건이어야 한다.");
        assertEquals(2, matchedOrderList.size(), "체결 거래 내역 수는 2건이어야 한다.");
    }

    @Test
    @DisplayName("요청 주문(Buy) 수량이 반대 주문(Sell) 수량보다 많을 경우, 반대 주문이 없거나 가격이 맞지 않아 매칭되지 않을 때까지 반복적으로 매칭 로직이 실행되어야 한다.")
    public void shouldRepeatMatchingForBuyOrderWithExcessQuantity() {
        // 매수 주문 (BUY)
        CreateMatchingCommand buyOrder = new CreateMatchingCommand(
                TRADING_PAIR,
                OrderType.BUY,
                BigDecimal.valueOf(10000),   // Price
                BigDecimal.valueOf(1.1),    // Quantity
                UUID.randomUUID(),  // User ID
                UUID.randomUUID()   // Order ID
        );

        orderMatchingService.matchOrders(buyOrder);

        // 미체결 주문 조회
        List<UnmatchedOrderA> unmatchedOrderAList = activatedOrderReader.findAll();

        // 체결 주문 조회
        List<MatchedOrder> matchedOrderList = completedOrderReader.findAll();

        assertEquals(6, unmatchedOrderAList.size(), "미체결 거래 내역 수는 6건이어야 한다.");
        assertEquals(8, matchedOrderList.size(), "체결 거래 내역 수는 8건이어야 한다.");
    }

    @Test
    @DisplayName("요청 주문(Sell) 수량이 반대 주문(Buy) 수량보다 많을 경우, 반대 주문이 없거나 가격이 맞지 않아 매칭되지 않을 때까지 반복적으로 매칭 로직이 실행되어야 한다.")
    public void shouldRepeatMatchingForSellOrderWithExcessQuantity() {
        // 매도 주문 (SELL)
        CreateMatchingCommand sellOrder = new CreateMatchingCommand(
                TRADING_PAIR,
                OrderType.SELL,
                BigDecimal.valueOf(8600),   // Price
                BigDecimal.valueOf(0.8),    // Quantity
                UUID.randomUUID(),  // User ID
                UUID.randomUUID()   // Order ID
        );

        orderMatchingService.matchOrders(sellOrder);

        // 미체결 주문 조회
        List<UnmatchedOrderA> unmatchedOrderAList = activatedOrderReader.findAll();

        // 체결 주문 조회
        List<MatchedOrder> matchedOrderList = completedOrderReader.findAll();

        assertEquals(5, unmatchedOrderAList.size(), "미체결 거래 내역 수는 5건이어야 한다.");
        assertEquals(8, matchedOrderList.size(), "체결 거래 내역 수는 8건이어야 한다.");
    }

    @Test
    @DisplayName("가격이 같으면 먼저 들어온 주문이 우선 체결(조회)되어야 한다.")
    @SkipSetUp
    void shouldPrioritizeEarlierOrderWhenPricesAreEqual() {
        // 동일 가격 그리고 생성 시간이 다른 두 매수 주문 생성

        // 먼저 들어온 주문
        UnmatchedOrderA earlierBuyOrder = UnmatchedOrderA.builder()
                .userId(UUID.randomUUID())
                .orderId(UUID.randomUUID())
                .tradingPair(TRADING_PAIR)
                .price(BigDecimal.valueOf(9000))
                .quantity(BigDecimal.valueOf(0.1))
                .type(OrderType.BUY)
                .createdAt(LocalDateTime.now())
                .build();

        // 나중에 들어온 주문
        UnmatchedOrderA laterBuyOrder = UnmatchedOrderA.builder()
                .userId(UUID.randomUUID())
                .orderId(UUID.randomUUID())
                .tradingPair(TRADING_PAIR)
                .price(BigDecimal.valueOf(9000))
                .quantity(BigDecimal.valueOf(0.1))
                .type(OrderType.BUY)
                .createdAt(LocalDateTime.now().plusMinutes(10))
                .build();

        activatedOrderStore.save(earlierBuyOrder);
        activatedOrderStore.save(laterBuyOrder);

        // 동일 조건의 주문 중 top 우선 주문(가격 오름차순, 생성 시간 오름차순) 조회
        Optional<UnmatchedOrderA> topOrderOpt = activatedOrderReader.findTopByTypeAndTradingPairOrderByPriceDescCreatedAtAsc(
                OrderType.BUY, TRADING_PAIR);

        // 먼저 들어온 주문(earlierBuyOrder)이 반환돼야 함
        assertTrue(topOrderOpt.isPresent(), "최상위 주문이 존재해야 한다.");
        UnmatchedOrderA topOrder = topOrderOpt.get();

        // 두 주문의 가격이 동일하므로, 생성 시간(createdAt)이 더 빠른 주문이 조회돼야 함
        assertEquals(earlierBuyOrder.getCreatedAt(), topOrder.getCreatedAt(), "가격이 동일하면 먼저 들어온 주문(생성 시간이 빠른 주문)이 우선 조회되어야 한다.");
    }
}
