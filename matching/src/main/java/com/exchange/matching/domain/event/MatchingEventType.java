package com.exchange.matching.domain.event;

/**
 * 매칭 이벤트 타입을 정의하는 열거형
 */
public enum MatchingEventType {
    ORDER_RECEIVED,      // 주문 접수
    ORDER_UNMATCHED,     // 미체결
    ORDER_MATCHED,       // 체결
    ORDER_REMAINING,     // 잔여 주문
    PROCESSING_COMPLETED, // 처리 완료
    ERROR // 에러
}
