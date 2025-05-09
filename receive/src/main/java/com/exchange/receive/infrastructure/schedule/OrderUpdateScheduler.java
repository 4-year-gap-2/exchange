package com.exchange.receive.infrastructure.schedule;

import com.exchange.receive.infrastructure.dto.KafkaOrderStoreEvent;
import com.exchange.receive.infrastructure.enums.OperationType;
import com.exchange.receive.infrastructure.enums.OrderType;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.*;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderUpdateScheduler {

    private final RedisTemplate<String, String> redisTemplate;
    private final StringRedisTemplate stringRedisTemplate;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final RedisConnectionFactory connectionFactory;

    private static final String ORDER_UPDATES_KEY_PREFIX = "v6d:order:pending-updates:";
    private static final String ORDER_UPDATES_INDEX_KEY = "v6d:order:pending-updates:index";
    private static final String ORDER_VERSIONS_KEY = "v6d:order:versions";
    private static final String KAFKA_TOPIC = "matching-to-order_completed.execute-order-unmatched";

    /**
     * 3초마다 실행되는 주문 업데이트 스케줄러
     */
    @Scheduled(fixedDelay = 3000)
    public void processOrderUpdates() {
        try {
            // Pending updates 인덱스 조회
            Set<String> orderIds = redisTemplate.opsForSet().members(ORDER_UPDATES_INDEX_KEY);

            if (orderIds == null || orderIds.isEmpty()) {
                return; // 처리할 업데이트가 없음
            }

            log.info("주문 업데이트 처리 시작: {} 건", orderIds.size());

            // 각 주문 ID별 데이터를 담을 맵
            Map<String, Map<String, String>> orderDataMap = new HashMap<>();

            // 최신 방식으로 파이프라인 실행
            RedisConnection connection = connectionFactory.getConnection();
            try {
                connection.openPipeline();

                // 모든 주문 ID에 대한 해시 데이터 조회 명령 큐에 넣기
                for (String orderId : orderIds) {
                    String updateKey = ORDER_UPDATES_KEY_PREFIX + orderId;
                    stringRedisTemplate.opsForHash().entries(updateKey);
                }

                // 파이프라인 실행 결과 받기
                List<Object> results = connection.closePipeline();

                // 결과 매핑
                int index = 0;
                for (String orderId : orderIds) {
                    @SuppressWarnings("unchecked")
                    Map<String, String> orderData = (Map<String, String>) results.get(index++);
                    if (orderData != null && !orderData.isEmpty()) {
                        orderDataMap.put(orderId, orderData);
                    }
                }
            } finally {
                connection.close();
            }

            // 수집된 데이터 처리
            for (Map.Entry<String, Map<String, String>> entry : orderDataMap.entrySet()) {
                String orderId = entry.getKey();
                Map<String, String> orderData = entry.getValue();

                try {
                    // 버전 정보 추출
                    int version = Integer.parseInt(orderData.getOrDefault("version", "1"));

                    // 이벤트 객체 생성
                    KafkaOrderStoreEvent event = KafkaOrderStoreEvent.builder()
                            .tradingPair(orderData.get("tradingPair"))
                            .orderType(OrderType.valueOf(orderData.get("orderType")))
                            .price(new BigDecimal(orderData.get("price")))
                            .quantity(new BigDecimal(orderData.get("quantity")))
                            .userId(UUID.fromString(orderData.get("userId")))
                            .orderId(UUID.fromString(orderData.get("orderId")))
                            .startTime(Long.parseLong(orderData.get("timestamp")))
                            .operationType(OperationType.valueOf(orderData.get("operation")))
                            .version(version)
                            .build();

                    // 최종 처리할 주문 ID와 버전
                    final String finalOrderId = orderId;
                    final int finalVersion = version;

                    // Kafka로 전송
                    CompletableFuture<SendResult<String, Object>> future =
                            kafkaTemplate.send(KAFKA_TOPIC, orderId, event);

                    // 비동기 처리 결과 핸들링
                    future.whenComplete((result, ex) -> {
                        if (ex == null) {
                            log.info("주문 업데이트 전송 성공: {}, operation: {}, version: {}",
                                    finalOrderId, orderData.get("operation"), finalVersion);

                            // 낙관적 락 구현 - 버전 확인 후 삭제
                            String updateKey = ORDER_UPDATES_KEY_PREFIX + finalOrderId;
                            removeIfVersionMatches(updateKey, finalOrderId, finalVersion);
                        } else {
                            log.error("주문 업데이트 전송 실패: {}", finalOrderId, ex);
                        }
                    });

                } catch (Exception e) {
                    log.error("주문 업데이트 처리 중 오류 발생: {}", orderId, e);
                }
            }

            log.info("주문 업데이트 처리 완료: {} 건", orderDataMap.size());

        } catch (Exception e) {
            log.error("주문 업데이트 스케줄러 오류", e);
        }
    }

    /**
     * 낙관적 락을 구현한 버전 확인 후 삭제 메서드
     * Spring Data Redis 트랜잭션을 사용
     */
    private void removeIfVersionMatches(String updateKey, String orderId, int expectedVersion) {
        try {
            // 현재 버전 조회
            Object versionObj = redisTemplate.opsForHash().get(ORDER_VERSIONS_KEY, orderId);
            String currentVersionStr = versionObj != null ? versionObj.toString() : null;

            int currentVersion = (currentVersionStr != null) ?
                    Integer.parseInt(currentVersionStr) : 1;

            // 버전 일치 확인
            if (currentVersion == expectedVersion) {
                // 트랜잭션을 사용하여 원자적으로 삭제 처리
                redisTemplate.execute(new SessionCallback<Object>() {
                    @Override
                    public Object execute(RedisOperations operations) throws DataAccessException {
                        try {
                            operations.multi(); // 트랜잭션 시작

                            // 버전 재확인 (트랜잭션 내에서)
                            operations.opsForHash().get(ORDER_VERSIONS_KEY, orderId);

                            // 트랜잭션 실행
                            List<Object> txResults = operations.exec();

                            // 트랜잭션이 성공하고, 결과가 있으면 버전 재확인
                            if (!txResults.isEmpty()) {
                                String txVersionStr = (String) txResults.get(0);
                                int txVersion = txVersionStr != null ?
                                        Integer.parseInt(txVersionStr) : 1;

                                // 버전이 여전히 일치하면 데이터 삭제
                                if (txVersion == expectedVersion) {
                                    stringRedisTemplate.delete(updateKey);
                                    stringRedisTemplate.opsForSet().remove(ORDER_UPDATES_INDEX_KEY, orderId);
                                    log.info("주문 업데이트 삭제 성공: {}, version: {}", orderId, expectedVersion);
                                    return true;
                                } else {
                                    // 버전이 변경되었으면 삭제하지 않음
                                    log.info("트랜잭션 내 버전 변경 감지로 삭제 취소: {}, expected: {}, current: {}",
                                            orderId, expectedVersion, txVersion);
                                    return false;
                                }
                            }
                            return false;
                        } catch (Exception e) {
                            log.error("트랜잭션 처리 중 오류: {}", orderId, e);
                            return false;
                        }
                    }
                });
            } else {
                // 버전이 일치하지 않으면 삭제하지 않음
                log.info("버전 불일치로 삭제 취소: {}, expected: {}, current: {}",
                        orderId, expectedVersion, currentVersion);
            }
        } catch (Exception e) {
            log.error("버전 확인 중 오류 발생: {}", orderId, e);
        }
    }
}