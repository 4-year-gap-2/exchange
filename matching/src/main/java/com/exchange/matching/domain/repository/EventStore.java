package com.exchange.matching.domain.repository;

import com.exchange.matching.domain.event.MatchingEvent;
import com.exchange.matching.domain.event.MatchingEventType;

import java.util.List;
import java.util.UUID;

/**
 * 이벤트 저장소 인터페이스
 */
public interface EventStore {
    /**
     * 이벤트 저장
     */
    void saveEvent(MatchingEvent event);

    /**
     * 특정 상관 관계 ID에 해당하는 모든 이벤트 조회
     */
    List<MatchingEvent> findEventsByCorrelationId(String correlationId);

    /**
     * 특정 상관 관계 ID와 이벤트 타입에 해당하는 이벤트 조회
     */
    List<MatchingEvent> findEventsByCorrelationIdAndType(String correlationId, MatchingEventType eventType);

    /**
     * 특정 이벤트 ID에 해당하는 이벤트 삭제
     */
    void deleteEvent(UUID eventId);

    /**
     * 특정 상관 관계 ID에 해당하는 모든 이벤트를 처리 완료로 표시 (또는 삭제)
     */
    void markEventsAsProcessed(String correlationId);
}
