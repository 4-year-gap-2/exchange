package com.exchange.matching.infrastructure.repository;

import com.exchange.matching.domain.event.MatchingEvent;
import com.exchange.matching.domain.event.MatchingEventType;
import com.exchange.matching.domain.repository.EventStore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Redis 기반 이벤트 저장소 구현
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class RedisEventStore implements EventStore {

    private static final String EVENT_KEY_PREFIX = "events:";
    private static final int EVENT_EXPIRY_DAYS = 7; // 이벤트 만료 기간

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void saveEvent(MatchingEvent event) {
        try {
            String eventJson = objectMapper.writeValueAsString(event);
            String eventKey = getEventKey(event.getCorrelationId(), event.getEventId());

            redisTemplate.opsForValue().set(eventKey, eventJson);
            redisTemplate.expire(eventKey, EVENT_EXPIRY_DAYS, TimeUnit.DAYS);

            log.debug("이벤트 저장: {} (타입: {})", event.getEventId(), event.getEventType());
        } catch (JsonProcessingException e) {
            log.error("이벤트 저장 실패", e);
            throw new RuntimeException("이벤트 저장 실패", e);
        }
    }

    @Override
    public List<MatchingEvent> findEventsByCorrelationId(String correlationId) {
        String pattern = EVENT_KEY_PREFIX + correlationId + ":*";

        Set<String> keys = redisTemplate.keys(pattern);
        if (keys.isEmpty()) {
            return new ArrayList<>();
        }

        return keys.stream()
                .map(key -> redisTemplate.opsForValue().get(key))
                .filter(Objects::nonNull)
                .map(this::deserializeEvent)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public List<MatchingEvent> findEventsByCorrelationIdAndType(String correlationId, MatchingEventType eventType) {
        return findEventsByCorrelationId(correlationId).stream()
                .filter(event -> event.getEventType() == eventType)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteEvent(UUID eventId) {
        // 모든 이벤트 키에서 해당 eventId를 가진 키 찾기
        String pattern = EVENT_KEY_PREFIX + "*:" + eventId;
        Set<String> keys = redisTemplate.keys(pattern);

        if (!keys.isEmpty()) {
            redisTemplate.delete(keys);
            log.debug("이벤트 삭제: {}", eventId);
        } else {
            log.warn("삭제할 이벤트를 찾을 수 없음: {}", eventId);
        }
    }

    @Override
    public void markEventsAsProcessed(String correlationId) {
        String pattern = EVENT_KEY_PREFIX + correlationId + ":*";
        Set<String> keys = redisTemplate.keys(pattern);

        if (!keys.isEmpty()) {
            redisTemplate.delete(keys);
            log.debug("처리 완료된 이벤트 삭제: {}, 개수: {}", correlationId, keys.size());
        } else {
            log.warn("처리 완료 표시할 이벤트가 없음: {}", correlationId);
        }
    }

    /**
     * 이벤트 JSON 문자열을 MatchingEvent 객체로 역직렬화
     */
    private MatchingEvent deserializeEvent(String eventJson) {
        try {
            return objectMapper.readValue(eventJson, MatchingEvent.class);
        } catch (JsonProcessingException e) {
            log.error("이벤트 역직렬화 실패", e);
            return null;
        }
    }

    /**
     * 상관 관계 ID와 이벤트 ID로 Redis 키 생성
     */
    private String getEventKey(String correlationId, UUID eventId) {
        return EVENT_KEY_PREFIX + correlationId + ":" + eventId;
    }
}