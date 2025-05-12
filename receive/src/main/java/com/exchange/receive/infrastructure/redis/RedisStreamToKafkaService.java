package com.exchange.receive.infrastructure.redis;

import com.exchange.receive.infrastructure.cassandra.ShardCalculator;
import com.exchange.receive.infrastructure.dto.KafkaMatchedOrderEvent;
import com.exchange.receive.infrastructure.dto.KafkaMatchingEvent;
import com.exchange.receive.infrastructure.dto.KafkaOrderStoreEvent;
import com.exchange.receive.infrastructure.enums.OperationType;
import com.exchange.receive.infrastructure.enums.OrderType;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.Subscription;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
@RequiredArgsConstructor
public class RedisStreamToKafkaService {

    private final ExecutorService kafkaExecutorService = Executors.newFixedThreadPool(20);

    private static final String MATCH_STREAM_KEY = "v6d:stream:matches";
    private static final String UNMATCH_STREAM_KEY = "v6d:stream:unmatched";
    private static final String PARTIAL_MATCHED_STREAM_KEY = "v6d:stream:partialMatched";
    private static final String MATCH_KAFKA_TOPIC = "matching-to-order_completed.execute-order-matched";
    private static final String UNMATCH_KAFKA_TOPIC = "matching-to-order_completed.execute-order-unmatched";
    private static final String PARTIAL_MATCHED_KAFKA_TOPIC = "user-to-matching.execute-order-delivery.v6d";

    private final RedisTemplate<String, String> redisTemplate;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final RedisStreamRecoveryService recoveryService;
    private final ShardCalculator shardCalculator;

    private StreamMessageListenerContainer<String, MapRecord<String, String, String>> listenerContainer;
    private Subscription matchSubscription;
    private Subscription unmatchSubscription;
    private Subscription partialMatchSubscription;

    private String consumerGroupName;
    private AtomicInteger pendingCount = new AtomicInteger(0);

    @PostConstruct
    public void init() throws UnknownHostException {
        // Consumer 그룹 및 이름 설정
        String hostname = InetAddress.getLocalHost().getHostName();
        String consumerName = hostname + "-" + System.currentTimeMillis();
        this.consumerGroupName = "matching-service-group";

        // Consumer 그룹 생성 (존재하지 않는 경우에만)
        createConsumerGroupIfNotExists(MATCH_STREAM_KEY, consumerGroupName);
        createConsumerGroupIfNotExists(UNMATCH_STREAM_KEY, consumerGroupName);
        createConsumerGroupIfNotExists(PARTIAL_MATCHED_STREAM_KEY, consumerGroupName);

        // Listener 컨테이너 설정
        StreamMessageListenerContainer.StreamMessageListenerContainerOptions<String, MapRecord<String, String, String>> options =
                StreamMessageListenerContainer.StreamMessageListenerContainerOptions
                        .builder()
                        .pollTimeout(Duration.ofMillis(100))
                        .batchSize(50)
                        .executor(Executors.newFixedThreadPool(10))
                        .errorHandler((e) -> log.info("Redis Streams Listener 종료"))
                        .build();

        this.listenerContainer = StreamMessageListenerContainer.create(
                Objects.requireNonNull(redisTemplate.getConnectionFactory()),
                options
        );

        // Match 스트림 구독
        Consumer consumer = Consumer.from(consumerGroupName, consumerName);

        this.matchSubscription = this.listenerContainer.receive(
                consumer,
                StreamOffset.create(MATCH_STREAM_KEY, ReadOffset.lastConsumed()),
                new MatchStreamListener()
        );

        this.unmatchSubscription = this.listenerContainer.receive(
                consumer,
                StreamOffset.create(UNMATCH_STREAM_KEY, ReadOffset.lastConsumed()),
                new UnmatchStreamListener()
        );

        this.partialMatchSubscription = this.listenerContainer.receive(
                consumer,
                StreamOffset.create(PARTIAL_MATCHED_STREAM_KEY, ReadOffset.lastConsumed()),
                new PartialStreamListener()
        );

        // 컨테이너 시작
        this.listenerContainer.start();
        log.info("Redis Stream Consumer 시작 - Group: {}, Consumer: {}", consumerGroupName, consumerName);

        // 복구 서비스 초기화
        recoveryService.initialize(consumerGroupName);
    }

    @PreDestroy
    public void shutdown() {
        log.info("Redis Stream Consumer 종료 중...");

        // 복구 서비스 종료
        recoveryService.shutdown();

        if (matchSubscription != null) {
            matchSubscription.cancel();
        }
        if (unmatchSubscription != null) {
            unmatchSubscription.cancel();
        }
        if (partialMatchSubscription != null) {
            partialMatchSubscription.cancel();
        }
        if (listenerContainer != null) {
            listenerContainer.stop();
        }
        log.info("Redis Stream Consumer 종료 완료");
    }

    private void createConsumerGroupIfNotExists(String streamKey, String groupName) {
        try {
            // 스트림이 존재하지 않으면 생성
            Boolean streamExists = redisTemplate.hasKey(streamKey);
            if (!streamExists) {
                Map<String, String> initialData = new HashMap<>();
                initialData.put("init", "true");
                redisTemplate.opsForStream().add(streamKey, initialData);
                log.info("스트림 생성: {}", streamKey);
            }

            // Consumer 그룹 생성 시도
            redisTemplate.opsForStream().createGroup(streamKey, groupName);
            log.info("Consumer 그룹 생성: {} - {}", streamKey, groupName);

        } catch (Exception e) {
            // 예외의 원인을 재귀적으로 검사
            Throwable cause = e;
            boolean isBusyGroupError = false;

            // 모든 중첩된 예외를 확인
            while (cause != null) {
                if (cause instanceof io.lettuce.core.RedisBusyException ||
                        (cause.getMessage() != null && cause.getMessage().contains("BUSYGROUP"))) {
                    isBusyGroupError = true;
                    break;
                }
                cause = cause.getCause();
            }

            if (isBusyGroupError) {
                log.info("Consumer 그룹 이미 존재: {} - {}", streamKey, groupName);
            } else {
                log.info("Consumer 그룹 생성 오류: {} - {}", streamKey, groupName, e);
            }
        }
    }

    /**
     * 매칭 스트림 리스너
     */
    private class MatchStreamListener implements StreamListener<String, MapRecord<String, String, String>> {
        @Override
        public void onMessage(MapRecord<String, String, String> message) {
            pendingCount.incrementAndGet();
            try {
                processMatchMessage(message);
            } finally {
                pendingCount.decrementAndGet();
            }
        }
    }

    /**
     * 미체결 스트림 리스너
     */
    private class UnmatchStreamListener implements StreamListener<String, MapRecord<String, String, String>> {
        @Override
        public void onMessage(MapRecord<String, String, String> message) {
            pendingCount.incrementAndGet();
            try {
                processUnmatchMessage(message);
            } finally {
                pendingCount.decrementAndGet();
            }
        }
    }

    /**
     * 부분 체결 스트림 리스너
     */
    private class PartialStreamListener implements StreamListener<String, MapRecord<String, String, String>> {
        @Override
        public void onMessage(MapRecord<String, String, String> message) {
            pendingCount.incrementAndGet();
            try {
                processPartialMatchMessage(message);
            } finally {
                pendingCount.decrementAndGet();
            }
        }
    }

    /**
     * 매칭 메시지 처리 및 Kafka로 전송
     */
    private void processMatchMessage(MapRecord<String, String, String> message) {
        try {
            String messageId = message.getId().getValue();
            Map<String, String> body = message.getValue();

            Instant instant = Instant.ofEpochSecond(Long.parseLong(body.get("timestamp")));
            LocalDate localDate = instant.atZone(ZoneId.systemDefault()).toLocalDate();

            // 매칭된 주문 정보를 하나의 DTO로 생성
            // TODO kafka 정확히 한번만 전송 되도록 처리 필요
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
            // matchId를 key로 사용하여 이벤트 전송
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
                    log.info("매칭 이벤트 Kafka 전송 완료: {}", messageId);
                } else {
                    // 실패 시 로그만 남김 (Redis에서 ACK 하지 않음)
                    log.error("매칭 이벤트 Kafka 전송 실패: {}", messageId, ex);
                }
            });
        } catch (Exception e) {
            log.error("매칭 메시지 처리 오류", e);
        }
    }

    /**
     * 미체결 메시지 처리 및 Kafka로 전송
     */
    private void processUnmatchMessage(MapRecord<String, String, String> message) {
        try {
            String messageId = message.getId().getValue();
            Map<String, String> body = message.getValue();

            long timestamp = Long.parseLong(body.get("timestamp"));
            long processedTimestamp = "BUY".equals(body.get("orderType"))
                    ? 9999999999999L - timestamp
                    : timestamp;

            Instant instant = Instant.ofEpochSecond(processedTimestamp);
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
                    .createdAt(instant)
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
                    log.info("미체결 메시지 Kafka 전송 완료: {}", messageId);
                } else {
                    // 실패 시 로그만 남김 (Redis에서 ACK 하지 않음)
                    log.info("미체결 메시지 Kafka 전송 실패: {}", messageId, ex);
                }
            });
        } catch (Exception e) {
            log.info("미체결 메시지 처리 오류", e);
        }
    }

    /**
     * 부분체결 메시지 처리 및 Kafka로 전송
     */
    private void processPartialMatchMessage(MapRecord<String, String, String> message) {
        try {
            String messageId = message.getId().getValue();
            Map<String, String> body = message.getValue();

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
                    log.info("부분체결 메시지 Kafka 전송 완료: {}", messageId);
                } else {
                    // 실패 시 로그만 남김 (Redis에서 ACK 하지 않음)
                    log.info("부분체결 메시지 Kafka 전송 실패: {}", messageId, ex);
                }
            });
        } catch (Exception e) {
            log.info("미체결 메시지 처리 오류", e);
        }
    }
}