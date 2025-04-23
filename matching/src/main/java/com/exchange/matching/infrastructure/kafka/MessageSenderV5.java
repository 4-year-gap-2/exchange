package com.exchange.matching.infrastructure.kafka;

import com.exchange.matching.domain.service.MatchingServiceV5.MatchingOrder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Kafka를 통해 주문 관련 메시지를 전송하는 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MessageSenderV5 {

    private static final String UNMATCHED_ORDER_TOPIC = "matching-to-matching.execute-unmatched-orders";
    private static final String MATCHED_ORDER_TOPIC = "matching-to-matching.execute-matched-orders";
    private static final String PROCESSING_COMPLETED_TOPIC = "matching-to-matching.execute-processing-completed";

    private final KafkaTemplate<String, Object> geneKafkaTemplate;
    private final ObjectMapper objectMapper;

    /**
     * 미체결 주문 메시지 전송
     */
    public void sendUnmatchedOrderMessage(MatchingOrder order, String correlationId) {
        try {
            UnmatchedOrderMessage message = new UnmatchedOrderMessage(
                    correlationId,
                    order.getTradingPair(),
                    order.getOrderType().toString(),
                    order.getPrice(),
                    order.getQuantity(),
                    order.getUserId().toString(),
                    order.getOrderId().toString(),
                    System.currentTimeMillis()
            );

            String messageJson = objectMapper.writeValueAsString(message);
            geneKafkaTemplate.send(UNMATCHED_ORDER_TOPIC, order.getOrderId().toString(), messageJson);

            log.debug("미체결 주문 메시지 전송 완료: {}", order.getOrderId());
        } catch (JsonProcessingException e) {
            log.error("미체결 주문 메시지 직렬화 실패", e);
            throw new RuntimeException("미체결 주문 메시지 전송 실패", e);
        }
    }

    /**
     * 체결 주문 메시지 전송
     */
    public void sendMatchedOrderMessage(MatchingOrder buyOrder, MatchingOrder sellOrder,
                                        BigDecimal matchedQuantity, BigDecimal executionPrice,
                                        String correlationId) {
        try {
            MatchedOrderMessage message = new MatchedOrderMessage(
                    correlationId,
                    buyOrder.getTradingPair(),
                    executionPrice,
                    matchedQuantity,
                    buyOrder.getUserId().toString(),
                    buyOrder.getOrderId().toString(),
                    sellOrder.getUserId().toString(),
                    sellOrder.getOrderId().toString(),
                    System.currentTimeMillis()
            );

            String messageJson = objectMapper.writeValueAsString(message);
            // 거래 ID를 키로 사용하여 동일 거래에 대한 메시지가 동일 파티션으로 전송되도록 함
            String key = buyOrder.getOrderId().toString() + "-" + sellOrder.getOrderId().toString();
            geneKafkaTemplate.send(MATCHED_ORDER_TOPIC, key, messageJson);

            log.debug("체결 주문 메시지 전송 완료: 매수={}, 매도={}",
                    buyOrder.getOrderId(), sellOrder.getOrderId());
        } catch (JsonProcessingException e) {
            log.error("체결 주문 메시지 직렬화 실패", e);
            throw new RuntimeException("체결 주문 메시지 전송 실패", e);
        }
    }

    /**
     * 처리 완료 메시지 전송
     */
    public void sendProcessingCompletedMessage(String correlationId) {
        try {
            ProcessingCompletedMessage message = new ProcessingCompletedMessage(
                    correlationId,
                    System.currentTimeMillis()
            );

            String messageJson = objectMapper.writeValueAsString(message);
            geneKafkaTemplate.send(PROCESSING_COMPLETED_TOPIC, correlationId, messageJson);

            log.debug("처리 완료 메시지 전송: {}", correlationId);
        } catch (JsonProcessingException e) {
            log.error("처리 완료 메시지 직렬화 실패", e);
            throw new RuntimeException("처리 완료 메시지 전송 실패", e);
        }
    }

    /**
     * 미체결 주문 메시지 구조
     */
    record UnmatchedOrderMessage(
            String correlationId,
            String tradingPair,
            String orderType,
            BigDecimal price,
            BigDecimal quantity,
            String userId,
            String orderId,
            long timestamp
    ) {}

    /**
     * 체결 주문 메시지 구조
     */
    record MatchedOrderMessage(
            String correlationId,
            String tradingPair,
            BigDecimal executionPrice,
            BigDecimal matchedQuantity,
            String buyUserId,
            String buyOrderId,
            String sellUserId,
            String sellOrderId,
            long timestamp
    ) {}

    /**
     * 처리 완료 메시지 구조
     */
    record ProcessingCompletedMessage(
            String correlationId,
            long timestamp
    ) {}
}