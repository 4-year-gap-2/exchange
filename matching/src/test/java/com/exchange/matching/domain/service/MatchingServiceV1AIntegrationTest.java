package com.exchange.matching.domain.service;

import com.exchange.matching.application.dto.enums.OrderStatus;
import com.exchange.matching.application.dto.enums.OrderType;
import com.exchange.matching.domain.entiry.Order;
import com.exchange.matching.domain.entiry.Transaction;
import com.exchange.matching.domain.repository.TransactionReader;
import com.exchange.matching.domain.repository.TransactionStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
    private TransactionReader transactionReader;

    @Autowired
    private TransactionStore transactionStore;

    private static final String TRADING_PAIR = "BTC/KRW";

    @BeforeEach
    public void setUp(TestInfo testInfo) throws InterruptedException {
        // 테스트 전에 거래 내역 테이블 초기화
        transactionStore.deleteAll();

        if (testInfo.getTestMethod().isPresent() && testInfo.getTestMethod().get().isAnnotationPresent(SkipSetUp.class)) {
            return;
        }

        // 테스트 데이터 초기화
        // 매수 주문 (BUY)
        Transaction buyOrder1 = TransactionFactory.createBuyOrder1();
        Transaction buyOrder2 = TransactionFactory.createBuyOrder2();
        Transaction buyOrder3 = TransactionFactory.createBuyOrder3();
        Transaction buyOrder4 = TransactionFactory.createBuyOrder4();
        // 매도 주문 (SELL)
        Transaction sellOrder1 = TransactionFactory.createSellOrder1();
        Transaction sellOrder2 = TransactionFactory.createSellOrder2();
        Transaction sellOrder3 = TransactionFactory.createSellOrder3();
        Transaction sellOrder4 = TransactionFactory.createSellOrder4();
        Transaction sellOrder5 = TransactionFactory.createSellOrder5();

        transactionStore.save(buyOrder1);
        Thread.sleep(500);

        List<Transaction> transactionList = List.of(buyOrder2, buyOrder3, buyOrder4,
                sellOrder1, sellOrder2, sellOrder3, sellOrder4, sellOrder5);

        transactionStore.saveAll(transactionList);
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
        // 가격 조건이 맞지 않아 어떤 주문도 체결되지 않고 미체결(PENDING) 상태로 거래 내역에 저장
        orderMatchingService.matchOrders(buyOrder1);
        orderMatchingService.matchOrders(buyOrder2);
        orderMatchingService.matchOrders(buyOrder3);
        orderMatchingService.matchOrders(buyOrder4);

        orderMatchingService.matchOrders(sellOrder1);
        orderMatchingService.matchOrders(sellOrder2);
        orderMatchingService.matchOrders(sellOrder3);
        orderMatchingService.matchOrders(sellOrder4);
        orderMatchingService.matchOrders(sellOrder5);

        // 저장된 거래 내역(Transactions) 조회
        List<Transaction> transactions = transactionReader.findAll();

        assertEquals(9, transactions.size(), "저장된 거래 내역 수는 9건이어야 합니다.");

        transactions.forEach(tx ->
                assertEquals(OrderStatus.PENDING, tx.getStatus(), "모든 거래 내역은 미체결 상태여야 합니다.")
        );
    }

    @Test
    @DisplayName("매도 주문과 매수 주문의 가격과 수량이 모두 일치하여 완전 체결되어야 한다.")
    public void testOrdersFullyMatched() {
        // 매도 주문 (SELL)
        Order sellOrder = Order.builder()
                .userId(UUID.randomUUID())
                .orderType(OrderType.SELL)
                .tradingPair(TRADING_PAIR)
                .price(BigDecimal.valueOf(9000))
                .quantity(BigDecimal.valueOf(0.1))
                .orderStatus(OrderStatus.PENDING)
                .build();

        orderMatchingService.matchOrders(sellOrder);

        // 저장된 거래 내역(Transactions) 조회
        List<Transaction> transactions = transactionReader.findAll();

        // 테스트 데이터 9건을 초기화했으므로, 매도 주문이 1건 추가되어 총 10건이 저장돼 있어야 함
        assertEquals(10, transactions.size(), "저장된 거래 내역 수는 10건이어야 한다.");

        long completedCount = transactions.stream()
                .filter(tx -> tx.getStatus() == OrderStatus.COMPLETED)
                .count();
        assertEquals(2, completedCount, "COMPLETED 상태의 거래 내역은 2건이어야 한다.");

        long matchedCount = transactions.stream()
                .filter(tx -> tx.getType() == OrderType.SELL && tx.getStatus() == OrderStatus.COMPLETED)
                .count();
        assertEquals(1, matchedCount, "매도 주문이 체결된 거래 내역은 1건이어야 한다.");

        long buyMatchedCount = transactions.stream()
                .filter(tx -> tx.getType() == OrderType.BUY && tx.getStatus() == OrderStatus.COMPLETED)
                .count();
        assertEquals(1, buyMatchedCount, "매수 주문이 체결된 거래 내역은 1건이어야 한다.");
    }

    @Test
    @DisplayName("요청 주문(Buy) 수량이 반대 주문(Sell) 수량보다 적을 경우, 요청 주문(Buy)은 체결되고 반대 주문(Sell)은 미체결 상태로 남아야 한다.")
    public void shouldPartiallyMatchSellOrder() {
        // 매수 주문 (BUY)
        Order buyOrder = Order.builder()
                .userId(UUID.randomUUID())
                .orderType(OrderType.BUY)
                .tradingPair(TRADING_PAIR)
                .price(BigDecimal.valueOf(9600))
                .quantity(BigDecimal.valueOf(0.1))
                .orderStatus(OrderStatus.PENDING)
                .build();

        orderMatchingService.matchOrders(buyOrder);

        // 저장된 거래 내역(Transactions) 조회
        List<Transaction> transactions = transactionReader.findAll();

        // 테스트 데이터 9건을 초기화했으므로, 매수 주문이 1건 추가되어 총 10건이 저장돼 있어야 함
        assertEquals(10, transactions.size(), "저장된 거래 내역 수는 10건이어야 한다.");

        long completedCount = transactions.stream()
                .filter(tx -> tx.getStatus() == OrderStatus.COMPLETED)
                .count();
        assertEquals(1, completedCount, "COMPLETED 상태의 거래 내역은 1건이어야 한다.");

        long matchedCount = transactions.stream()
                .filter(tx -> tx.getType() == OrderType.BUY && tx.getStatus() == OrderStatus.COMPLETED)
                .count();
        assertEquals(1, matchedCount, "매수 주문이 체결된 거래 내역은 1건이어야 한다.");

        long sellMatchedCount = transactions.stream()
                .filter(tx -> tx.getType() == OrderType.SELL && tx.getStatus() == OrderStatus.COMPLETED)
                .count();
        assertEquals(0, sellMatchedCount, "매도 주문이 체결된 거래 내역은 없어야 한다.");
    }

    @Test
    @DisplayName("요청 주문(Sell) 수량이 반대 주문(Buy) 수량보다 적을 경우, 요청 주문(Sell)은 체결되고 반대 주문(Buy)은 미체결 상태로 남아야 한다.")
    public void shouldPartiallyMatchBuyOrder() {
        // 매도 주문 (SELL)
        Order sellOrder = Order.builder()
                .userId(UUID.randomUUID())
                .orderType(OrderType.SELL)
                .tradingPair(TRADING_PAIR)
                .price(BigDecimal.valueOf(8800))
                .quantity(BigDecimal.valueOf(0.05))
                .orderStatus(OrderStatus.PENDING)
                .build();

        orderMatchingService.matchOrders(sellOrder);

        // 저장된 거래 내역(Transactions) 조회
        List<Transaction> transactions = transactionReader.findAll();

        // 테스트 데이터 9건을 초기화했으므로, 매도 주문이 1건 추가되어 총 10건이 저장돼 있어야 함
        assertEquals(10, transactions.size(), "저장된 거래 내역 수는 10건이어야 합니다.");

        long completedCount = transactions.stream()
                .filter(tx -> tx.getStatus() == OrderStatus.COMPLETED)
                .count();
        assertEquals(1, completedCount, "COMPLETED 상태의 거래 내역은 1건이어야 한다.");

        long matchedCount = transactions.stream()
                .filter(tx -> tx.getType() == OrderType.SELL && tx.getStatus() == OrderStatus.COMPLETED)
                .count();
        assertEquals(1, matchedCount, "매도 주문이 체결된 거래 내역은 1건이어야 한다.");

        long buyMatchedCount = transactions.stream()
                .filter(tx -> tx.getType() == OrderType.BUY && tx.getStatus() == OrderStatus.COMPLETED)
                .count();
        assertEquals(0, buyMatchedCount, "매수 주문이 체결된 거래 내역은 없어야 한다.");
    }

    @Test
    @DisplayName("요청 주문(Buy) 수량이 반대 주문(Sell) 수량보다 많을 경우, 매칭 로직이 반복적으로 실행되어 가능한 만큼 체결하고, 남은 주문은 부분 체결로 남아야 한다.")
    public void shouldRepeatMatchingForBuyOrderWithExcessQuantity() {
        // 매수 주문 (BUY)
        Order buyOrder = Order.builder()
                .userId(UUID.randomUUID())
                .orderType(OrderType.BUY)
                .tradingPair(TRADING_PAIR)
                .price(BigDecimal.valueOf(10000))
                .quantity(BigDecimal.valueOf(1.1))
                .orderStatus(OrderStatus.PENDING)
                .build();

        orderMatchingService.matchOrders(buyOrder);

        // 저장된 거래 내역(Transactions) 조회
        List<Transaction> transactions = transactionReader.findAll();

        // 테스트 데이터 9건을 초기화했으므로, 매수 주문이 1건 추가되어 총 10건이 저장돼 있어야 함
        assertEquals(10, transactions.size(), "저장된 거래 내역 수는 10건이어야 한다.");

        long completedCount = transactions.stream()
                .filter(tx -> tx.getStatus() == OrderStatus.COMPLETED)
                .count();
        assertEquals(4, completedCount, "COMPLETED 상태의 거래 내역은 4건이어야 한다.");

        long matchedCount = transactions.stream()
                .filter(tx -> tx.getType() == OrderType.SELL && tx.getStatus() == OrderStatus.COMPLETED)
                .count();
        assertEquals(3, matchedCount, "매도 주문이 체결된 거래 내역은 3건이어야 한다.");

        long buyMatchedCount = transactions.stream()
                .filter(tx -> tx.getType() == OrderType.BUY && tx.getStatus() == OrderStatus.COMPLETED)
                .count();
        assertEquals(1, buyMatchedCount, "매수 주문이 체결된 거래 내역은 1건이어야 한다.");
    }

    @Test
    @DisplayName("요청 주문(Sell) 수량이 반대 주문(Buy) 수량보다 많을 경우, 매칭 로직이 반복적으로 실행되어 가능한 만큼 체결하고, 남은 주문은 부분 체결로 남아야 한다.")
    public void shouldRepeatMatchingForSellOrderWithExcessQuantity() {
        // 매도 주문 (SELL)
        Order sellOrder = Order.builder()
                .userId(UUID.randomUUID())
                .orderType(OrderType.SELL)
                .tradingPair(TRADING_PAIR)
                .price(BigDecimal.valueOf(8600))
                .quantity(BigDecimal.valueOf(0.8))
                .orderStatus(OrderStatus.PENDING)
                .build();

        orderMatchingService.matchOrders(sellOrder);

        // 저장된 거래 내역(Transactions) 조회
        List<Transaction> transactions = transactionReader.findAll();

        // 테스트 데이터 9건을 초기화했으므로, 매도 주문이 1건 추가되어 총 10건이 저장돼 있어야 함
        assertEquals(10, transactions.size(), "저장된 거래 내역 수는 10건이어야 한다.");

        long completedCount = transactions.stream()
                .filter(tx -> tx.getStatus() == OrderStatus.COMPLETED)
                .count();
        assertEquals(5, completedCount, "COMPLETED 상태의 거래 내역은 5건이어야 한다.");

        long matchedCount = transactions.stream()
                .filter(tx -> tx.getType() == OrderType.BUY && tx.getStatus() == OrderStatus.COMPLETED)
                .count();
        assertEquals(4, matchedCount, "매수 주문이 체결된 거래 내역은 4건이어야 한다.");

        long sellMatchedCount = transactions.stream()
                .filter(tx -> tx.getType() == OrderType.SELL && tx.getStatus() == OrderStatus.COMPLETED)
                .count();
        assertEquals(1, sellMatchedCount, "매도 주문이 체결된 거래 내역은 1건이어야 한다.");
    }

    @Test
    @DisplayName("가격이 같으면 먼저 들어온 주문이 우선 체결(조회)되어야 한다.")
    @SkipSetUp
    void shouldPrioritizeEarlierOrderWhenPricesAreEqual() throws InterruptedException {
        // 동일 가격 그리고 생성 시간이 다른 두 매수 주문 생성

        // 먼저 들어온 주문
        Transaction earlierBuyOrder = Transaction.builder()
                .userId(UUID.randomUUID())
                .tradingPair(TRADING_PAIR)
                .price(BigDecimal.valueOf(9000))
                .quantity(BigDecimal.valueOf(0.1))
                .type(OrderType.BUY)
                .status(OrderStatus.PENDING)
                .build();

        // 나중에 들어온 주문
        Transaction laterBuyOrder = Transaction.builder()
                .userId(UUID.randomUUID())
                .tradingPair(TRADING_PAIR)
                .price(BigDecimal.valueOf(9000))
                .quantity(BigDecimal.valueOf(0.1))
                .type(OrderType.BUY)
                .status(OrderStatus.PENDING)
                .build();

        transactionStore.save(earlierBuyOrder);
        Thread.sleep(500);
        transactionStore.save(laterBuyOrder);

        // 동일 조건의 주문 중 top 우선 주문(가격 오름차순, 생성 시간 오름차순) 조회
        Optional<Transaction> topOrderOpt = transactionReader.findTopByTypeAndTradingPairAndStatusOrderByPriceDescCreatedAtAsc(
                OrderType.BUY, TRADING_PAIR, OrderStatus.PENDING);

        // 먼저 들어온 주문(earlierBuyOrder)이 반환돼야 함
        assertTrue(topOrderOpt.isPresent(), "최상위 주문이 존재해야 한다.");
        Transaction topOrder = topOrderOpt.get();

        // 두 주문의 가격이 동일하므로, 생성 시간(createdAt)이 더 빠른 주문이 조회돼야 함
        assertEquals(earlierBuyOrder.getCreatedAt(), topOrder.getCreatedAt(), "가격이 동일하면 먼저 들어온 주문(생성 시간이 빠른 주문)이 우선 조회되어야 한다.");
    }
}
