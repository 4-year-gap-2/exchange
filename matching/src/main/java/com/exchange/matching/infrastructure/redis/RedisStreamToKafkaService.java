package com.exchange.matching.infrastructure.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.lettuce.core.RedisException;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.Subscription;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
@RequiredArgsConstructor
public class RedisStreamToKafkaService {

    private static final String MATCH_STREAM_KEY = "v6:stream:matches";
    private static final String UNMATCH_STREAM_KEY = "v6:stream:unmatched";
    private static final String MATCH_KAFKA_TOPIC = "matching-to-matching.execute-matching-callback-test";
    private static final String UNMATCH_KAFKA_TOPIC = "matching-to-matching.execute-unmatched-callback-test";

    private final RedisTemplate<String, String> redisTemplate;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    private StreamMessageListenerContainer<String, MapRecord<String, String, String>> listenerContainer;
    private Subscription matchSubscription;
    private Subscription unmatchSubscription;

    private String consumerGroupName;

    private AtomicInteger pendingCount = new AtomicInteger(0);
    private volatile boolean shuttingDown = false;

    @PostConstruct
    public void init() throws UnknownHostException {
        // Consumer 그룹 및 이름 설정
        String hostname = InetAddress.getLocalHost().getHostName();
        String consumerName = hostname + "-" + System.currentTimeMillis();
        this.consumerGroupName = "matching-service-group";

        // Consumer 그룹 생성 (존재하지 않는 경우에만)
        createConsumerGroupIfNotExists(MATCH_STREAM_KEY, consumerGroupName);
        createConsumerGroupIfNotExists(UNMATCH_STREAM_KEY, consumerGroupName);

        // Listener 컨테이너 설정
        StreamMessageListenerContainer.StreamMessageListenerContainerOptions<String, MapRecord<String, String, String>> options =
                StreamMessageListenerContainer.StreamMessageListenerContainerOptions
                        .builder()
                        .pollTimeout(Duration.ofSeconds(1))
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

        // 컨테이너 시작
        this.listenerContainer.start();
        log.info("Redis Stream Consumer 시작 - Group: {}, Consumer: {}", consumerGroupName, consumerName);

        // Pending 메시지 처리 스케줄러 시작
        new Thread(this::processPendingMessages).start();
    }

    @PreDestroy
    public void shutdown() {
        log.info("Redis Stream Consumer 종료 중...");
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

        if (matchSubscription != null) {
            matchSubscription.cancel();
        }
        if (unmatchSubscription != null) {
            unmatchSubscription.cancel();
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
     * 주기적으로 Pending 메시지를 처리하는 스레드
     */
    private void processPendingMessages() {
        while (!shuttingDown) {
            try {
                // Match 스트림의 Pending 메시지 처리
                processPendingMessagesForStream(MATCH_STREAM_KEY);

                // Unmatch 스트림의 Pending 메시지 처리
                processPendingMessagesForStream(UNMATCH_STREAM_KEY);

                // 30초 대기
                Thread.sleep(30000);
            } catch (Exception e) {
                log.info("Pending 메시지 처리 중 오류", e);
            }
        }
    }

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

                        // 메시지 다시 처리
                        if (MATCH_STREAM_KEY.equals(streamKey)) {
                            processRawMatchMessage(record);
                        } else {
                            processRawUnmatchMessage(record);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.info("{} 스트림의 Pending 메시지 처리 중 오류", streamKey, e);
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
     * 매칭 메시지 처리 및 Kafka로 전송
     */
    private void processMatchMessage(MapRecord<String, String, String> message) {
        try {
            String messageId = message.getId().getValue();
            Map<String, String> body = message.getValue();

            // Kafka로 메시지 전송
            String jsonMessage = objectMapper.writeValueAsString(body);
            CompletableFuture<SendResult<String, Object>> future =
                    kafkaTemplate.send(MATCH_KAFKA_TOPIC, body.get("tradingPair"), jsonMessage);

            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    // 성공 시 Redis에서 ACK 처리
                    redisTemplate.opsForStream().acknowledge(MATCH_STREAM_KEY, consumerGroupName, messageId);
                    log.info("매칭 메시지 Kafka 전송 완료: {}", messageId);
                } else {
                    // 실패 시 로그만 남김 (Redis에서 ACK 하지 않음)
                    log.info("매칭 메시지 Kafka 전송 실패: {}", messageId, ex);
                }
            });
        } catch (Exception e) {
            log.info("매칭 메시지 처리 오류", e);
        }
    }

    /**
     * 미체결 메시지 처리 및 Kafka로 전송
     */
    private void processUnmatchMessage(MapRecord<String, String, String> message) {
        try {
            String messageId = message.getId().getValue();
            Map<String, String> body = message.getValue();

            // Kafka로 메시지 전송
            String jsonMessage = objectMapper.writeValueAsString(body);
            CompletableFuture<SendResult<String, Object>> future =
                    kafkaTemplate.send(UNMATCH_KAFKA_TOPIC, body.get("tradingPair"), jsonMessage);

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
     * 일반 MapRecord 처리 (pending 메시지 처리용)
     */
    private void processRawMatchMessage(MapRecord<String, Object, Object> record) {
        try {
            String messageId = record.getId().getValue();
            Map<String, String> body = convertMapEntriesToMap(record.getValue());

            // Kafka로 메시지 전송
            String jsonMessage = objectMapper.writeValueAsString(body);
            CompletableFuture<SendResult<String, Object>> future =
                    kafkaTemplate.send(MATCH_KAFKA_TOPIC, body.get("tradingPair"), jsonMessage);

            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    // 성공 시 Redis에서 ACK 처리
                    redisTemplate.opsForStream().acknowledge(MATCH_STREAM_KEY, consumerGroupName, messageId);
                    log.info("원시 매칭 메시지 Kafka 전송 완료: {}", messageId);
                } else {
                    // 실패 시 로그만 남김 (Redis에서 ACK 하지 않음)
                    log.info("원시 매칭 메시지 Kafka 전송 실패: {}", messageId, ex);
                }
            });
        } catch (Exception e) {
            log.info("원시 매칭 메시지 처리 오류", e);
        }
    }

    /**
     * 일반 MapRecord 처리 (pending 메시지 처리용)
     */
    private void processRawUnmatchMessage(MapRecord<String, Object, Object> record) {
        try {
            String messageId = record.getId().getValue();
            Map<String, String> body = convertMapEntriesToMap(record.getValue());

            // Kafka로 메시지 전송
            String jsonMessage = objectMapper.writeValueAsString(body);
            CompletableFuture<SendResult<String, Object>> future =
                    kafkaTemplate.send(UNMATCH_KAFKA_TOPIC, body.get("tradingPair"), jsonMessage);

            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    // 성공 시 Redis에서 ACK 처리
                    redisTemplate.opsForStream().acknowledge(UNMATCH_STREAM_KEY, consumerGroupName, messageId);
                    log.info("원시 미체결 메시지 Kafka 전송 완료: {}", messageId);
                } else {
                    // 실패 시 로그만 남김 (Redis에서 ACK 하지 않음)
                    log.info("원시 미체결 메시지 Kafka 전송 실패: {}", messageId, ex);
                }
            });
        } catch (Exception e) {
            log.info("원시 미체결 메시지 처리 오류", e);
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