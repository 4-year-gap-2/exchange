package com.exchange.matching.infrastructure.external;

import com.exchange.matching.domain.event.MatchingEvent;
import com.exchange.matching.domain.event.MatchingEventType;
import com.exchange.matching.domain.repository.EventStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 이벤트 발행 및 관리를 담당하는 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EventPublisherV5 {

    private final EventStore eventStore;

    /**
     * 이벤트 발행 (저장)
     */
    public void publish(MatchingEvent event) {
        eventStore.saveEvent(event);
        log.debug("이벤트 발행: {} (타입: {}, 상관 관계ID: {})",
                event.getEventId(), event.getEventType(), event.getCorrelationId());
    }

    /**
     * 특정 상관 관계 ID와 타입에 해당하는 이벤트 삭제
     */
    public void deleteEvent(String correlationId, MatchingEventType eventType) {
        List<MatchingEvent> events = eventStore.findEventsByCorrelationIdAndType(correlationId, eventType);

        if (events.isEmpty()) {
            log.warn("삭제할 이벤트가 없음: {} (타입: {})", correlationId, eventType);
            return;
        }

        for (MatchingEvent event : events) {
            eventStore.deleteEvent(event.getEventId());
            log.debug("이벤트 삭제: {} (타입: {}, 상관 관계ID: {})",
                    event.getEventId(), event.getEventType(), event.getCorrelationId());
        }
    }

    /**
     * 처리 완료 표시 및 관련 이벤트 정리
     */
    public void markProcessingCompleted(String correlationId) {
        // 처리 완료 이벤트 저장
        MatchingEvent completedEvent = MatchingEvent.processingCompleted(correlationId);
        eventStore.saveEvent(completedEvent);

        log.debug("처리 완료 표시: {}", correlationId);
    }

    /**
     * 모든 이벤트를 처리 완료로 표시 (또는 삭제)
     */
    public void markEventsAsProcessed(String correlationId) {
        eventStore.markEventsAsProcessed(correlationId);
        log.debug("모든 이벤트 처리 완료 표시: {}", correlationId);
    }
}