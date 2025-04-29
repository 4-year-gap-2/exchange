package com.exchange.order_completed.application.service;

import com.exchange.order_completed.application.command.CreateMatchedOrderStoreCommand;
import com.exchange.order_completed.common.exception.DuplicateMatchedOrderInformationException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OrderCompletionLockTest {

    @Autowired
    private OrderCompletedService orderCompletedService;

    @Autowired
    private RedissonClient redissonClient;

    private final UUID orderId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();

    private CreateMatchedOrderStoreCommand buildCreateOrderStoreCommand () {
        return CreateMatchedOrderStoreCommand.builder()
                .userId(userId)
                .orderId(orderId)
                .price(BigDecimal.TEN)
                .quantity(BigDecimal.ONE)
                .orderType("BUY")
                .tradingPair("BTC-USD")
                .build();
    }

    @BeforeAll
    void beforeAll() {
        // 테스트 전 Redis, DB 초기화
        redissonClient.getKeys().flushall();
//        completedOrderStore.deleteAll();
    }

    @Test
    @DisplayName("동시 주문 완료 요청 시, 락을 통해 중복 저장이 방지되어야 한다.")
    void whenConcurrentCompleteOrder_thenOnlyOneSucceeds() throws InterruptedException, ExecutionException {
        CreateMatchedOrderStoreCommand command = buildCreateOrderStoreCommand();
        int attempt = 1;

        int threadCount = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch readyLatch = new CountDownLatch(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        List<Future<Boolean>> results = new ArrayList<>();

        // 각 스레드는 startLatch.await() 후 동시에 실행
        for (int i = 0; i < threadCount; i++) {
            results.add(executor.submit(() -> {
                readyLatch.countDown();
                startLatch.await();

                try {
                    orderCompletedService.completeMatchedOrder(command, attempt);
                    return true;    // 정상 저장된 스레드
                } catch (DuplicateMatchedOrderInformationException e) {
                    return false;   // 락 실패 or 이미 처리된 경우
                }
            }));
        }

        // 모든 스레드 준비 완료 → 동시에 시작
        readyLatch.await();
        startLatch.countDown();

        // 결과 집계
        long successCount = 0;
        for (Future<Boolean> f : results) {
            if (f.get()) successCount++;
        }

        // 한 번만 성공, 나머지는 false
        assertEquals(1, successCount, "하나의 스레드만 주문 완료를 수행해야 한다.");
    }
}
