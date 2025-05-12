package com.exchange.receive.infrastructure.redis;

import com.exchange.receive.infrastructure.cassandra.ShardCalculator;
import com.exchange.receive.infrastructure.dto.KafkaMatchedOrderEvent;
import com.exchange.receive.infrastructure.dto.KafkaMatchingEvent;
import com.exchange.receive.infrastructure.dto.KafkaOrderStoreEvent;
import com.exchange.receive.infrastructure.enums.OperationType;
import com.exchange.receive.infrastructure.enums.OrderType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.PendingMessage;
import org.springframework.data.redis.connection.stream.PendingMessages;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
@RequiredArgsConstructor
public class RedisStreamRecoveryService {

    private final ExecutorService kafkaExecutorService = Executors.newFixedThreadPool(20);

    private static final String MATCH_STREAM_KEY = "v6d:stream:matches";
    private static final String UNMATCH_STREAM_KEY = "v6d:stream:unmatched";
    private static final String PARTIAL_MATCHED_STREAM_KEY = "v6d:stream:partialMatched";
    private static final String MATCH_KAFKA_TOPIC = "matching-to-order_completed.execute-order-matched";
    private static final String UNMATCH_KAFKA_TOPIC = "matching-to-order_completed.execute-order-unmatched";
    private static final String PARTIAL_MATCHED_KAFKA_TOPIC = "user-to-matching.execute-order-delivery.v6d";

    private final RedisTemplate<String, String> redisTemplate;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ShardCalculator shardCalculator;

    private String consumerGroupName;
    private AtomicInteger pendingCount = new AtomicInteger(0);
    private volatile boolean shuttingDown = false;

    /**
     * 서비스 초기화
     */
    public void initialize(String consumerGroupName) {
        this.consumerGroupName = consumerGroupName;
        // Pending 메시지 처리 스케줄러 시작
        new Thread(this::processPendingMessages).start();
        log.info("Redis Stream Recovery Service 초기화 완료 - Group: {}", consumerGroupName);
    }

    /**
     * 서비스 종료
     */
    public void shutdown() {
        log.info("Redis Stream Recovery Service 종료 중...");
        shuttingDown = true;

        // 모든 pending 메시지가 처리될 때까지 대기 (최대 30초)
        int maxWaitSeconds = 30;
        for (int i = 0; i < maxWaitSeconds; i++) {
            int count = pendingCount.get();
            if (count <= 0) {
                break;
            }
            log.info("Pending 메시지 처리 대기 중: {}", count);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        // Executor service 종료
        kafkaExecutorService.shutdown();
        log.info("Redis Stream Recovery Service 종료 완료");
    }

    /**
     * 주기적으로 Pending 메시지를 처리하는 스레드
     */
    private void processPendingMessages() {
        while (!shuttingDown) {
            try {
                // Match 스트림의 Pending 메시지 처리
                processPendingMessagesForStream(MATCH_STREAM_KEY);

                // Unmatch 스트림의 Pending 메시지 처리
                processPendingMessagesForStream(UNMATCH_STREAM_KEY);

                // 부분 체결 스트림의 Pending 메시지 처리
                processPendingMessagesForStream(PARTIAL_MATCHED_STREAM_KEY);

                // 30초 대기
                Thread.sleep(30000);
            } catch (Exception e) {
                log.error("Pending 메시지 처리 중 오류", e);
            }
        }
    }

    /**
     * 특정 스트림의 Pending 메시지 처리
     */
    private void processPendingMessagesForStream(String streamKey) {
        try {
            // 처리되지 않은 메시지 조회 (생성 후 5분 이상 경과된 메시지)
            PendingMessages pendingMessages = redisTemplate.opsForStream()
                    .pending(streamKey, consumerGroupName, Range.unbounded(), 100);

            if (pendingMessages.isEmpty()) {
                return;
            }

            log.info("{} 스트림에서 처리되지 않은 메시지 {} 개 발견", streamKey, pendingMessages.size());

            for (PendingMessage pending : pendingMessages) {
                // 10분(600,000ms) 이상 지난 메시지만 재처리
                if (pending.getElapsedTimeSinceLastDelivery().compareTo(Duration.ofMinutes(10)) > 0) {
                    String messageId = pending.getIdAsString();
                    log.info("Pending 메시지 재처리: {} (지연: {}ms)", messageId, pending.getElapsedTimeSinceLastDelivery());

                    // 메시지 다시 가져오기
                    List<MapRecord<String, Object, Object>> records = redisTemplate.opsForStream()
                            .range(streamKey, Range.closed(messageId, messageId));

                    if (records != null && !records.isEmpty()) {
                        MapRecord<String, Object, Object> record = records.get(0);

                        // 메시지 다시 처리 (스트림에 따라 다른 처리)
                        pendingCount.incrementAndGet();
                        try {
                            if (MATCH_STREAM_KEY.equals(streamKey)) {
                                processRawMatchMessage(record);
                            } else if (UNMATCH_STREAM_KEY.equals(streamKey)) {
                                processRawUnmatchMessage(record);
                            } else if (PARTIAL_MATCHED_STREAM_KEY.equals(streamKey)) {
                                processRawPartialMatchMessage(record);
                            }
                        } finally {
                            pendingCount.decrementAndGet();
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("{} 스트림의 Pending 메시지 처리 중 오류", streamKey, e);
        }
    }

    /**
     * 일반 MapRecord 처리 (pending 매칭 메시지 처리용)
     */
    private void processRawMatchMessage(MapRecord<String, Object, Object> record) {
        try {
            String messageId = record.getId().getValue();
            Map<String, String> body = convertMapEntriesToMap(record.getValue());

            Instant instant = Instant.ofEpochSecond(Long.parseLong(body.get("timestamp")));
            LocalDate localDate = instant.atZone(ZoneId.systemDefault()).toLocalDate();

            // 매칭된 주문 정보를 하나의 DTO로 생성
            KafkaMatchedOrderEvent matchedEvent = KafkaMatchedOrderEvent.builder()
                    .tradingPair(body.get("tradingPair"))
                    .executionPrice(new BigDecimal(body.get("executionPrice")))
                    .matchedQuantity(new BigDecimal(body.get("matchedQuantity")))
                    .buyUserId(UUID.fromString(body.get("buyUserId")))
                    .sellUserId(UUID.fromString(body.get("sellUserId")))
                    .buyMatchedOrderId(UUID.randomUUID())
                    .sellMatchedOrderId(UUID.randomUUID())
                    .createdAt(instant)
                    .yearMonthDate(localDate)
                    .buyShard(shardCalculator.calculateShard(UUID.fromString(body.get("buyOrderId"))))
                    .sellShard(shardCalculator.calculateShard(UUID.fromString(body.get("sellOrderId"))))
                    .build();

            // 매칭 이벤트를 Kafka로 전송
            CompletableFuture<SendResult<String, Object>> future =
                    CompletableFuture.supplyAsync(() -> {
                        try {
                            return kafkaTemplate.send(MATCH_KAFKA_TOPIC, matchedEvent).get();
                        } catch (Exception e) {
                            throw new CompletionException(e);
                        }
                    }, kafkaExecutorService);

            // 전송 완료 후 처리
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    // 성공 시 Redis에서 ACK 처리
                    redisTemplate.opsForStream().acknowledge(MATCH_STREAM_KEY, consumerGroupName, messageId);
                    log.info("원시 매칭 메시지 Kafka 전송 완료: {}", messageId);
                } else {
                    // 실패 시 로그만 남김 (Redis에서 ACK 하지 않음)
                    log.error("원시 매칭 메시지 Kafka 전송 실패: {}", messageId, ex);
                }
            });
        } catch (Exception e) {
            log.error("원시 매칭 메시지 처리 오류", e);
        }
    }

    /**
     * 일반 MapRecord 처리 (pending 미체결 메시지 처리용)
     */
    private void processRawUnmatchMessage(MapRecord<String, Object, Object> record) {
        try {
            String messageId = record.getId().getValue();
            Map<String, String> body = convertMapEntriesToMap(record.getValue());

            Instant instant = Instant.ofEpochSecond(Long.parseLong(body.get("timestamp")));
            LocalDate localDate = instant.atZone(ZoneId.systemDefault()).toLocalDate();

            KafkaOrderStoreEvent event = KafkaOrderStoreEvent.builder()
                    .tradingPair(body.get("tradingPair"))
                    .orderType(OrderType.valueOf(body.get("orderType")))
                    .price(new BigDecimal(body.get("price")))
                    .quantity(new BigDecimal(body.get("quantity")))
                    .userId(UUID.fromString(body.get("userId")))
                    .orderId(UUID.fromString(body.get("orderId")))
                    .startTime(Long.parseLong(body.get("timestamp")))
                    .operationType(OperationType.valueOf(body.get("operation")))
                    .shard(shardCalculator.calculateShard(UUID.fromString(body.get("orderId"))))
                    .yearMonthDate(localDate)
                    .build();

            // Kafka로 메시지 전송
            CompletableFuture<SendResult<String, Object>> future =
                    CompletableFuture.supplyAsync(() -> {
                        try {
                            return kafkaTemplate.send(UNMATCH_KAFKA_TOPIC, body.get("orderId"), event).get();
                        } catch (Exception e) {
                            throw new CompletionException(e);
                        }
                    }, kafkaExecutorService);

            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    // 성공 시 Redis에서 ACK 처리
                    redisTemplate.opsForStream().acknowledge(UNMATCH_STREAM_KEY, consumerGroupName, messageId);
                    log.info("원시 미체결 메시지 Kafka 전송 완료: {}", messageId);
                } else {
                    // 실패 시 로그만 남김 (Redis에서 ACK 하지 않음)
                    log.error("원시 미체결 메시지 Kafka 전송 실패: {}", messageId, ex);
                }
            });
        } catch (Exception e) {
            log.error("원시 미체결 메시지 처리 오류", e);
        }
    }

    /**
     * 일반 MapRecord 처리 (pending 부분체결 메시지 처리용)
     */
    private void processRawPartialMatchMessage(MapRecord<String, Object, Object> record) {
        try {
            String messageId = record.getId().getValue();
            Map<String, String> body = convertMapEntriesToMap(record.getValue());

            KafkaMatchingEvent event = KafkaMatchingEvent.builder()
                    .tradingPair(body.get("tradingPair"))
                    .orderType(OrderType.valueOf(body.get("orderType")))
                    .price(new BigDecimal(body.get("price")))
                    .quantity(new BigDecimal(body.get("quantity")))
                    .userId(UUID.fromString(body.get("userId")))
                    .orderId(UUID.fromString(body.get("orderId")))
                    .build();

            // Kafka로 메시지 전송
            CompletableFuture<SendResult<String, Object>> future =
                    kafkaTemplate.send(PARTIAL_MATCHED_KAFKA_TOPIC, body.get("orderId"), event);

            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    // 성공 시 Redis에서 ACK 처리
                    redisTemplate.opsForStream().acknowledge(PARTIAL_MATCHED_STREAM_KEY, consumerGroupName, messageId);
                    log.info("원시 부분체결 메시지 Kafka 전송 완료: {}", messageId);
                } else {
                    // 실패 시 로그만 남김 (Redis에서 ACK 하지 않음)
                    log.error("원시 부분체결 메시지 Kafka 전송 실패: {}", messageId, ex);
                }
            });
        } catch (Exception e) {
            log.error("원시 부분체결 메시지 처리 오류", e);
        }
    }

    /**
     * Map<Object, Object>를 Map<String, String>으로 변환
     */
    private Map<String, String> convertMapEntriesToMap(Map<Object, Object> entries) {
        Map<String, String> result = new HashMap<>();

        for (Map.Entry<Object, Object> entry : entries.entrySet()) {
            result.put(entry.getKey().toString(), entry.getValue().toString());
        }

        return result;
    }
}