//package com.exchange.order_completed.application.service;
//
//import com.exchange.order_completed.domain.entity.cassandra.entity.MatchedOrder;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.data.cassandra.core.CassandraTemplate;
//import org.springframework.data.cassandra.core.InsertOptions;
//
//import java.math.BigDecimal;
//import java.time.LocalDateTime;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.UUID;
//import java.util.concurrent.*;
//import java.util.stream.IntStream;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//@SpringBootTest
//@DisplayName("경량 트랜잭션 동시성 테스트")
//class LightweightTransactionConcurrencyTest {
//
//    @Autowired
//    private CassandraTemplate cassandraTemplate;
//
//    private MatchedOrder buildOrder() {
//        return MatchedOrder.builder()
//                .userId(UUID.randomUUID())
//                .orderId(UUID.randomUUID())
//                .createdAt(LocalDateTime.now())
//                .price(BigDecimal.TEN)
//                .quantity(BigDecimal.ONE)
//                .orderType("BUY")
//                .tradingPair("BTC-USD")
//                .build();
//    }
//
//    @Test
//    @DisplayName("PK(userId, orderId, createdAt)가 스레드별로 같은 경우 경량 트랜잭션을 적용하면 동시 insert 시 한 건만 성공해야 한다.")
//    void testIfNotExistsIsAtomic() throws Exception {
//        MatchedOrder order = buildOrder();
//
//        // LWT(Lightweight Transaction) 옵션
//        InsertOptions options = InsertOptions.builder()
//                .withIfNotExists()
//                .build();
//
//        int threads = 2;
//        ExecutorService exec = Executors.newFixedThreadPool(threads);
//        CountDownLatch readyLatch = new CountDownLatch(threads);
//        CountDownLatch startLatch = new CountDownLatch(1);
//
//        // 동시에 insert 시도하여 Future<Boolean> 리스트 수집
//        List<Future<Boolean>> results = IntStream.range(0, threads)
//                .mapToObj(i -> exec.submit(() -> {
//                    readyLatch.countDown();      // 준비 완료
//                    startLatch.await();          // 모두 준비될 때까지 대기
//                    return cassandraTemplate.insert(order, options).wasApplied();   // true: 성공, false: 이미 존재
//                }))
//                .toList();
//
//        // 두 스레드 준비가 끝나면 동시에 시작
//        readyLatch.await();
//        startLatch.countDown();
//
//        // 3) 결과 집계: true(성공) 가 딱 1개여야 한다
//        long appliedCount = 0;
//        for (Future<Boolean> f : results) {
//            if (f.get(5, TimeUnit.SECONDS)) {
//                appliedCount++;
//            }
//        }
//
//        // get(0) 이 false 면 “경쟁에서 진 것” 이므로 동시 시도가 있었을 가능성이 큼
//        // get(0) 이 true 이면 “경쟁에서 이긴 것” 이지만, 순차적으로 실행됐을 수도 있음
//        System.out.println(results.get(0).get());
//        System.out.println(results.get(1).get());
//
//        assertEquals(1, appliedCount, "LWT가 정상 작동했다면, 성공한 스레드는 1개여야 한다.");
//
//        // 두 스레드 결과가 서로 달라야 경합(race)이 있었다고 볼 수 있음
//        assertNotEquals(results.get(0).get(), results.get(1).get(), "두 스레드의 wasApplied() 결과는 서로 달라야 한다.");
//    }
//
//    private MatchedOrder buildOrder(UUID userId, UUID orderId, LocalDateTime createdAt) {
//        return MatchedOrder.builder()
//                .userId(userId)
//                .orderId(orderId)
//                .createdAt(createdAt)
//                .price(BigDecimal.TEN)
//                .quantity(BigDecimal.ONE)
//                .orderType("BUY")
//                .tradingPair("BTC-USD")
//                .build();
//    }
//
//    @Test
//    @DisplayName("createdAt이 스레드별로 다르면 복합 PK가 달라지므로 CAS가 성공하여 두 건 모두 삽입에 성공해야 한다.")
//    void testIfNotExistsIsAtomic_withDistinctCreatedAt() throws Exception {
//        // 공통 PK(userId, orderId, createdAt) 중 createdAt만 스레드별로 다르게 설정
//        // 사전 검증 시 사용되는 컬럼은 userId, orderId이기 때문에 createdAt을 다르게 설정하여 테스트
//        UUID userId = UUID.randomUUID();
//        UUID orderId = UUID.randomUUID();
//        LocalDateTime ts1 = LocalDateTime.now();
//        LocalDateTime ts2 = ts1.plusNanos(1_000_000); // 1ms 차이
//
//        // LWT(Lightweight Transaction) 옵션
//        InsertOptions options = InsertOptions.builder()
//                .withIfNotExists()
//                .build();
//
//        int threads = 2;
//        ExecutorService exec = Executors.newFixedThreadPool(threads);
//        CountDownLatch readyLatch = new CountDownLatch(threads);
//        CountDownLatch startLatch = new CountDownLatch(1);
//
//        List<Future<Boolean>> results = IntStream.range(0, threads)
//                .mapToObj(i -> exec.submit(() -> {
//                    readyLatch.countDown();    // 준비 완료 신호
//                    startLatch.await();        // 모두 준비될 때까지 대기
//
//                    // 스레드마다 다른 createdAt 설정
//                    LocalDateTime createdAt = (i == 0 ? ts1 : ts2);
//                    MatchedOrder order = buildOrder(userId, orderId, createdAt);
//
//                    return cassandraTemplate.insert(order, options).wasApplied();
//                }))
//                .toList();
//
//        // 동시에 시작
//        readyLatch.await();
//        startLatch.countDown();
//
//        // 결과 집계
//        List<Boolean> applied = new ArrayList<>();
//        for (Future<Boolean> f : results) {
//            applied.add(f.get(5, TimeUnit.SECONDS));
//        }
//        exec.shutdownNow();
//
//        System.out.println("thread0 applied? " + applied.get(0));
//        System.out.println("thread1 applied? " + applied.get(1));
//
//        // CAS IF NOT EXISTS 가 PK를 키로 판단하는데 PK에 포함되는 createdAt의 값이 다르므로, 모두 성공(true, true)이 나와야 함
//        assertTrue(applied.stream().allMatch(b -> b), "createdAt 이 다르면 두 번 모두 insert 성공해야 한다.");
//    }
//}
