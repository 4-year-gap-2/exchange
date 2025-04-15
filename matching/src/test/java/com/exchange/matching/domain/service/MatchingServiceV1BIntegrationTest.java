package com.exchange.matching.domain.service;

import com.exchange.matching.application.dto.enums.OrderType;
import com.exchange.matching.application.service.MatchingServiceV1B;
import com.exchange.matching.domain.entiry.ActivatedOrderB;
import com.exchange.matching.domain.entiry.CompletedOrder;
import com.exchange.matching.domain.entiry.Order;
import com.exchange.matching.domain.repository.ActivatedOrderBReader;
import com.exchange.matching.domain.repository.ActivatedOrderBStore;
import com.exchange.matching.domain.repository.CompletedOrderReader;
import com.exchange.matching.domain.repository.CompletedOrderStore;
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
public class MatchingServiceV1BIntegrationTest {

    @Autowired
    private MatchingServiceV1B orderMatchingService;

    @Autowired
    private ActivatedOrderBReader activatedOrderBReader;

    @Autowired
    private ActivatedOrderBStore activatedOrderBStore;

    @Autowired
    private CompletedOrderReader completedOrderReader;

    @Autowired
    private CompletedOrderStore completedOrderStore;

    private static final String TRADING_PAIR = "BTC/KRW";

    @BeforeEach
    public void setUp(TestInfo testInfo) {
        // 테스트 전에 거래 내역 테이블 초기화
        activatedOrderBStore.deleteAll();
        completedOrderStore.deleteAll();

        if (testInfo.getTestMethod().isPresent() && testInfo.getTestMethod().get().isAnnotationPresent(SkipSetUp.class)) {
            return;
        }

        // 테스트 데이터 초기화
        // 매수 주문 (BUY)
        ActivatedOrderB buyOrder1 = ActivatedOrderBFactory.createBuyOrder1();
        ActivatedOrderB buyOrder2 = ActivatedOrderBFactory.createBuyOrder2();
        ActivatedOrderB buyOrder3 = ActivatedOrderBFactory.createBuyOrder3();
        ActivatedOrderB buyOrder4 = ActivatedOrderBFactory.createBuyOrder4();
        // 매도 주문 (SELL)
        ActivatedOrderB sellOrder1 = ActivatedOrderBFactory.createSellOrder1();
        ActivatedOrderB sellOrder2 = ActivatedOrderBFactory.createSellOrder2();
        ActivatedOrderB sellOrder3 = ActivatedOrderBFactory.createSellOrder3();
        ActivatedOrderB sellOrder4 = ActivatedOrderBFactory.createSellOrder4();
        ActivatedOrderB sellOrder5 = ActivatedOrderBFactory.createSellOrder5();

        List<ActivatedOrderB> activatedOrderBList = List.of(buyOrder1, buyOrder2, buyOrder3, buyOrder4,
                sellOrder1, sellOrder2, sellOrder3, sellOrder4, sellOrder5);

        activatedOrderBStore.saveAll(activatedOrderBList);
    }

    @Test
    @SkipSetUp
    @DisplayName("주문 매칭이 실패하여 모든 주문이 미체결 상태로 남아야 한다.")
    public void testOrdersRemainPendingWhenNoMatchingOccurs() {
        // 매수 주문 (BUY)
        Order buyOrder1 = OrderFactory.createBuyOrder1();
        Order buyOrder2 = OrderFactory.createBuyOrder2();
        Order buyOrder3 = OrderFactory.createBuyOrder3();
        Order buyOrder4 = OrderFactory.createBuyOrder4();

        // 매도 주문 (SELL)
        Order sellOrder1 = OrderFactory.createSellOrder1();
        Order sellOrder2 = OrderFactory.createSellOrder2();
        Order sellOrder3 = OrderFactory.createSellOrder3();
        Order sellOrder4 = OrderFactory.createSellOrder4();
        Order sellOrder5 = OrderFactory.createSellOrder5();

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
        List<ActivatedOrderB> activatedOrderBList = activatedOrderBReader.findAll();

        // 체결 주문 조회
        List<CompletedOrder> completedOrderList = completedOrderReader.findAll();

        assertEquals(9, activatedOrderBList.size(), "미체결 거래 내역 수는 9건이어야 한다.");
        assertEquals(0, completedOrderList.size(), "체결 거래 내역 수는 0건이어야 한다.");
    }

    @Test
    @DisplayName("매도 주문과 매수 주문의 가격과 수량이 모두 일치하여 양쪽 주문 모두 완전 체결되어야 한다.")
    public void testOrdersFullyMatched() {
        // 매도 주문 (SELL)
        Order sellOrder = Order.builder()
                .userId(UUID.randomUUID())
                .orderType(OrderType.SELL)
                .tradingPair(TRADING_PAIR)
                .price(BigDecimal.valueOf(9000))
                .quantity(BigDecimal.valueOf(0.1))
                .build();

        orderMatchingService.matchOrders(sellOrder);

        // 미체결 주문 조회
        List<ActivatedOrderB> activatedOrderBList = activatedOrderBReader.findAll();

        // 체결 주문 조회
        List<CompletedOrder> completedOrderList = completedOrderReader.findAll();

        assertEquals(8, activatedOrderBList.size(), "미체결 거래 내역 수는 8건이어야 한다.");
        assertEquals(2, completedOrderList.size(), "체결 거래 내역 수는 2건이어야 한다.");
    }

    @Test
    @DisplayName("요청 주문(Buy) 수량이 반대 주문(Sell) 수량보다 적을 경우, 요청 주문(Buy)은 완전 체결되고 반대 주문(Sell)은 미체결 상태로 남아야 한다.")
    public void shouldPartiallyMatchSellOrder() {
        // 매수 주문 (BUY)
        Order buyOrder = Order.builder()
                .userId(UUID.randomUUID())
                .orderType(OrderType.BUY)
                .tradingPair(TRADING_PAIR)
                .price(BigDecimal.valueOf(9600))
                .quantity(BigDecimal.valueOf(0.1))
                .build();

        orderMatchingService.matchOrders(buyOrder);

        // 미체결 주문 조회
        List<ActivatedOrderB> activatedOrderBList = activatedOrderBReader.findAll();

        // 체결 주문 조회
        List<CompletedOrder> completedOrderList = completedOrderReader.findAll();

        assertEquals(9, activatedOrderBList.size(), "미체결 거래 내역 수는 9건이어야 한다.");
        assertEquals(2, completedOrderList.size(), "체결 거래 내역 수는 2건이어야 한다.");
    }

    @Test
    @DisplayName("요청 주문(Sell) 수량이 반대 주문(Buy) 수량보다 적을 경우, 요청 주문(Sell)은 완전 체결되고 반대 주문(Buy)은 미체결 상태로 남아야 한다.")
    public void shouldPartiallyMatchBuyOrder() {
        // 매도 주문 (SELL)
        Order sellOrder = Order.builder()
                .userId(UUID.randomUUID())
                .orderType(OrderType.SELL)
                .tradingPair(TRADING_PAIR)
                .price(BigDecimal.valueOf(8800))
                .quantity(BigDecimal.valueOf(0.05))
                .build();

        orderMatchingService.matchOrders(sellOrder);

        // 미체결 주문 조회
        List<ActivatedOrderB> activatedOrderBList = activatedOrderBReader.findAll();

        // 체결 주문 조회
        List<CompletedOrder> completedOrderList = completedOrderReader.findAll();

        assertEquals(9, activatedOrderBList.size(), "미체결 거래 내역 수는 9건이어야 한다.");
        assertEquals(2, completedOrderList.size(), "체결 거래 내역 수는 2건이어야 한다.");
    }

    @Test
    @DisplayName("요청 주문(Buy) 수량이 반대 주문(Sell) 수량보다 많을 경우, 반대 주문이 없거나 가격이 맞지 않아 매칭되지 않을 때까지 반복적으로 매칭 로직이 실행되어야 한다.")
    public void shouldRepeatMatchingForBuyOrderWithExcessQuantity() {
        // 매수 주문 (BUY)
        Order buyOrder = Order.builder()
                .userId(UUID.randomUUID())
                .orderType(OrderType.BUY)
                .tradingPair(TRADING_PAIR)
                .price(BigDecimal.valueOf(10000))
                .quantity(BigDecimal.valueOf(1.1))
                .build();

        orderMatchingService.matchOrders(buyOrder);

        // 미체결 주문 조회
        List<ActivatedOrderB> activatedOrderBList = activatedOrderBReader.findAll();

        // 체결 주문 조회
        List<CompletedOrder> completedOrderList = completedOrderReader.findAll();

        assertEquals(6, activatedOrderBList.size(), "미체결 거래 내역 수는 6건이어야 한다.");
        assertEquals(8, completedOrderList.size(), "체결 거래 내역 수는 8건이어야 한다.");
    }

    @Test
    @DisplayName("요청 주문(Sell) 수량이 반대 주문(Buy) 수량보다 많을 경우, 반대 주문이 없거나 가격이 맞지 않아 매칭되지 않을 때까지 반복적으로 매칭 로직이 실행되어야 한다.")
    public void shouldRepeatMatchingForSellOrderWithExcessQuantity() {
        // 매도 주문 (SELL)
        Order sellOrder = Order.builder()
                .userId(UUID.randomUUID())
                .orderType(OrderType.SELL)
                .tradingPair(TRADING_PAIR)
                .price(BigDecimal.valueOf(8600))
                .quantity(BigDecimal.valueOf(0.8))
                .build();

        orderMatchingService.matchOrders(sellOrder);

        // 미체결 주문 조회
        List<ActivatedOrderB> activatedOrderBList = activatedOrderBReader.findAll();

        // 체결 주문 조회
        List<CompletedOrder> completedOrderList = completedOrderReader.findAll();

        assertEquals(5, activatedOrderBList.size(), "미체결 거래 내역 수는 5건이어야 한다.");
        assertEquals(8, completedOrderList.size(), "체결 거래 내역 수는 8건이어야 한다.");
    }

    @Test
    @DisplayName("가격이 같으면 먼저 들어온 주문이 우선 체결(조회)되어야 한다.")
    @SkipSetUp
    void shouldPrioritizeEarlierOrderWhenPricesAreEqual() {
        // 동일 가격 그리고 생성 시간이 다른 두 매수 주문 생성

        // 먼저 들어온 주문
        ActivatedOrderB earlierBuyOrder = ActivatedOrderB.builder()
                .userId(UUID.randomUUID())
                .tradingPair(TRADING_PAIR)
                .price(BigDecimal.valueOf(9000))
                .quantity(BigDecimal.valueOf(0.1))
                .type(OrderType.BUY)
                .createdAt(LocalDateTime.now())
                .build();

        // 나중에 들어온 주문
        ActivatedOrderB laterBuyOrder = ActivatedOrderB.builder()
                .userId(UUID.randomUUID())
                .tradingPair(TRADING_PAIR)
                .price(BigDecimal.valueOf(9000))
                .quantity(BigDecimal.valueOf(0.1))
                .type(OrderType.BUY)
                .createdAt(LocalDateTime.now().plusMinutes(10))
                .build();

        activatedOrderBStore.save(earlierBuyOrder);
        activatedOrderBStore.save(laterBuyOrder);

        // 동일 조건의 주문 중 top 우선 주문(가격 오름차순, 생성 시간 오름차순) 조회
        Optional<ActivatedOrderB> topOrderOpt = activatedOrderBReader.findTopByTypeAndTradingPairOrderByPriceDescCreatedAtAsc(
                OrderType.BUY, TRADING_PAIR);

        // 먼저 들어온 주문(earlierBuyOrder)이 반환돼야 함
        assertTrue(topOrderOpt.isPresent(), "최상위 주문이 존재해야 한다.");
        ActivatedOrderB topOrder = topOrderOpt.get();

        // 두 주문의 가격이 동일하므로, 생성 시간(createdAt)이 더 빠른 주문이 조회돼야 함
        assertEquals(earlierBuyOrder.getCreatedAt(), topOrder.getCreatedAt(), "가격이 동일하면 먼저 들어온 주문(생성 시간이 빠른 주문)이 우선 조회되어야 한다.");
    }
}
