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
import org.springframework.data.redis.connection.RedisHashCommands;
import org.springframework.data.redis.core.*;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderUpdateScheduler {

    private final RedisTemplate<String, String> redisTemplate;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private StringRedisTemplate stringRedisTemplate;

    private static final String ORDER_UPDATES_KEY_PREFIX = "v6d:order:pending-updates:";
    private static final String ORDER_UPDATES_INDEX_KEY = "v6d:order:pending-updates:index";
    private static final String KAFKA_TOPIC = "matching-to-order_completed.execute-order-unmatched";

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

            // RedisTemplate의 executePipelined 메서드를 사용하여 리팩토링
            List<Object> results = redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
                RedisHashCommands hashCommands = connection.hashCommands();

                // 각 주문 ID에 대해 RedisHashCommands의 hGetAll 명령 실행
                for (String orderId : orderIds) {
                    byte[] keyBytes = (ORDER_UPDATES_KEY_PREFIX + orderId).getBytes(StandardCharsets.UTF_8);
                    hashCommands.hGetAll(keyBytes);
                }

                // executePipelined 메서드에서는 null을 반환해야 합니다
                return null;
            });

            // 결과 매핑
            int index = 0;
            for (String orderId : orderIds) {
                if (index >= results.size()) {
                    log.warn("파이프라인 결과 인덱스 초과: {}/{}", index, results.size());
                    break;
                }

                @SuppressWarnings("unchecked")
                Map<byte[], byte[]> orderDataBytes = (Map<byte[], byte[]>) results.get(index++);

                if (orderDataBytes != null && !orderDataBytes.isEmpty()) {
                    // 바이트 배열을 문자열로 변환
                    Map<String, String> stringMap = new HashMap<>();
                    for (Map.Entry<byte[], byte[]> entry : orderDataBytes.entrySet()) {
                        String key = new String(entry.getKey(), StandardCharsets.UTF_8);
                        String value = entry.getValue() != null ?
                                new String(entry.getValue(), StandardCharsets.UTF_8) : null;
                        stringMap.put(key, value);
                    }
                    orderDataMap.put(orderId, stringMap);
                }
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
     * 낙관적 락을 사용한 버전 확인 및 삭제 - RedisHashCommands 사용
     */
    private void removeIfVersionMatches(String updateKey, String orderId, int expectedVersion) {
        redisTemplate.execute(new RedisCallback<Boolean>() {
            @Override
            public Boolean doInRedis(RedisConnection connection) throws DataAccessException {
                try {
                    // RedisHashCommands 인터페이스 얻기
                    RedisHashCommands hashCommands = connection.hashCommands();

                    // updateKey를 감시
                    byte[] keyBytes = updateKey.getBytes(StandardCharsets.UTF_8);
                    connection.watch(keyBytes);

                    // 버전 정보 조회 (RedisHashCommands 사용)
                    byte[] versionFieldBytes = "version".getBytes(StandardCharsets.UTF_8);
                    byte[] versionValueBytes = hashCommands.hGet(keyBytes, versionFieldBytes);

                    String currentVersionStr = versionValueBytes != null ?
                            new String(versionValueBytes, StandardCharsets.UTF_8) : null;
                    int currentVersion = (currentVersionStr != null) ?
                            Integer.parseInt(currentVersionStr) : 1;

                    // 버전 불일치 시 트랜잭션 중단
                    if (currentVersion != expectedVersion) {
                        connection.unwatch();
                        log.info("버전 불일치로 삭제 취소: {}, expected: {}, current: {}",
                                orderId, expectedVersion, currentVersion);
                        return false;
                    }

                    // 버전 일치 시 트랜잭션 실행
                    connection.multi();

                    // 키 삭제
                    connection.del(keyBytes);
                    connection.sRem(ORDER_UPDATES_INDEX_KEY.getBytes(StandardCharsets.UTF_8),
                            orderId.getBytes(StandardCharsets.UTF_8));

                    // 트랜잭션 커밋
                    List<Object> results = connection.exec();

                    // 트랜잭션 성공 여부 확인
                    boolean success = (results != null && !results.isEmpty());
                    if (success) {
                        log.info("주문 업데이트 삭제 성공: {}, version: {}", orderId, expectedVersion);
                    } else {
                        log.info("동시성 충돌로 삭제 실패: {}", orderId);
                    }

                    return success;
                } catch (Exception e) {
                    log.error("버전 확인 중 오류 발생: {}", orderId, e);
                    return false;
                }
            }
        });
    }
}