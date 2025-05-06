//package com.exchange.order_completed.application.service;
//
//import com.exchange.order_completed.domain.entity.MatchedOrder;
//import com.exchange.order_completed.infrastructure.cassandra.repository.MatchedOrderReaderRepository;
//import com.exchange.order_completed.infrastructure.cassandra.repository.MatchedOrderStoreRepository;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.data.cassandra.core.CassandraTemplate;
//import org.springframework.data.cassandra.core.query.Criteria;
//import org.springframework.data.cassandra.core.query.Query;
//
//import java.math.BigDecimal;
//import java.time.Instant;
//import java.time.LocalDateTime;
//import java.time.ZoneId;
//import java.util.List;
//import java.util.UUID;
//import java.util.concurrent.*;
//import java.util.stream.IntStream;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//
//@SpringBootTest
//class MatchedOrderConcurrencyTest {
//
//    @Autowired
//    private MatchedOrderReaderRepository readerRepository;
//
//    @Autowired
//    private MatchedOrderStoreRepository storeRepository;
//
//    @Autowired
//    private CassandraTemplate cassandraTemplate;    // 직접 쿼리용
//
//    @Test
//    @DisplayName("사전 검증을 하지만 동시에 실행되면 두 건 모두 저장돼야 한다.")
//    void whenConcurrentCheckThenSave_withoutLwt_thenDuplicatesCreated() throws Exception {
//        UUID userId = UUID.randomUUID();
//        UUID orderId = UUID.randomUUID();
//
//        // Find 끝나고 동시에 Save 직전까지 막아줄 Barrier
//        CyclicBarrier barrier = new CyclicBarrier(2);
//        ExecutorService exec = Executors.newFixedThreadPool(2);
//
//        Instant base = Instant.now();
//        Instant ts1 = base;                            // Thread #0
//        Instant ts2 = base.plusMillis(1);   // Thread #1 (+1ms 차이)
//
//        List<Future<Void>> futures = IntStream.range(0, 2).mapToObj(i -> exec.<Void>submit(() -> {
//            // 동시 find
//            MatchedOrder existing = readerRepository.findByUserIdAndOrderId(userId, orderId);
//            barrier.await();    // 두 스레드 모두 find 끝날 때까지 대기
//
//            if (existing != null) {
//                return null;    // 이미 있으면 종료
//            }
//
//            Instant chooseTs = (i == 0 ? ts1 : ts2);
//            MatchedOrder order = MatchedOrder.builder()
//                    .userId(userId)
//                    .orderId(orderId)
//                    .createdAt(LocalDateTime.ofInstant(chooseTs, ZoneId.systemDefault()))
//                    .price(BigDecimal.TEN)
//                    .quantity(BigDecimal.ONE)
//                    .orderType("BUY")
//                    .tradingPair("BTC-USD")
//                    .build();
//
//            storeRepository.save(order);
//            return null;
//        })).toList();
//
//        // 태스크 완료 대기
//        for (Future<Void> f : futures) {
//            f.get(5, TimeUnit.SECONDS);
//        }
//        exec.shutdownNow();
//
//        // 검증
//        List<MatchedOrder> all = cassandraTemplate.select(
//                Query.query(
//                        Criteria.where("user_id").is(userId),
//                        Criteria.where("order_id").is(orderId)
//                ),
//                MatchedOrder.class
//        );
//        assertEquals(2, all.size(), "멱등성 보장에 실패하여 두 row가 생성돼야 한다.");
//    }
//}
