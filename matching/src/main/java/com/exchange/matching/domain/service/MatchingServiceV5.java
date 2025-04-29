package com.exchange.matching.domain.service;

import com.exchange.matching.application.command.CreateMatchingCommand;
import com.exchange.matching.application.dto.enums.OrderType;
import com.exchange.matching.domain.event.MatchingEvent;
import com.exchange.matching.domain.event.MatchingEventType;
import com.exchange.matching.infrastructure.kafka.EventPublisherV5;
import com.exchange.matching.infrastructure.kafka.MessageSenderV5;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class MatchingServiceV5 implements MatchingService {

    private static final String SELL_ORDER_KEY = "v5:orders:sell:";
    private static final String BUY_ORDER_KEY = "v5:orders:buy:";

    private final RedisTemplate<String, String> redisTemplate;
    private final RedisScript<List<Object>> matchingScript;
    private final EventPublisherV5 eventPublisherV5;
    private final MessageSenderV5 messageSenderV5; // Kafka 메시지 전송을 위한 서비스

    public MatchingServiceV5(RedisTemplate<String, String> redisTemplate,
                             EventPublisherV5 eventPublisherV5,
                             MessageSenderV5 messageSenderV5) {
        this.redisTemplate = redisTemplate;
        this.eventPublisherV5 = eventPublisherV5;
        this.messageSenderV5 = messageSenderV5;

        // Lua 스크립트 로드
        DefaultRedisScript<List<Object>> script = new DefaultRedisScript<>();
        script.setResultType((Class) List.class);
        try {
            ClassPathResource resource = new ClassPathResource("scripts/matchingV4.lua");
            String scriptText = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            script.setScriptText(scriptText);
        } catch (IOException e) {
            log.error("Lua 스크립트 로드 실패", e);
            throw new RuntimeException("Lua 스크립트 로드 실패", e);
        }
        this.matchingScript = script;
    }

    @Override
    public void matchOrders(CreateMatchingCommand command) {
        // 상관 관계 ID 생성 (거래 흐름 추적용)
        String correlationId = UUID.randomUUID().toString();

        MatchingOrder matchingOrder = MatchingOrder.fromCommand(command);

        log.info("{} 주문접수 : {}원 {}개 (주문ID: {})",
                matchingOrder.getOrderType(), matchingOrder.getPrice(),
                matchingOrder.getQuantity(), matchingOrder.getUserId());

        // 복구를 위한 주문 접수 이벤트 발행
        MatchingEvent orderReceivedEvent = MatchingEvent.orderReceived(matchingOrder, correlationId);
        eventPublisherV5.publish(orderReceivedEvent);

        try {
            matchingProcess(matchingOrder, correlationId);

            // 모든 프로세스 정상 종료 기록된 이벤트 모두 제거 (완료 이벤트 발행 후)
            MatchingEvent completedEvent = MatchingEvent.processingCompleted(correlationId);
            eventPublisherV5.publish(completedEvent);

            // Kafka로 처리 완료 메시지 전송
            messageSenderV5.sendProcessingCompletedMessage(correlationId);

            // 이벤트 처리 완료 표시 (이벤트 삭제 또는 상태 변경)
            eventPublisherV5.markEventsAsProcessed(correlationId);
        } catch (Exception e) {
            log.error("주문 매칭 처리 중 오류 발생: {}", e.getMessage(), e);
            // 오류가 발생해도 이벤트는 이미 저장되어 있으므로 복구 가능
        }
    }

    /**
     * 주문 매칭 프로세스 시작
     */
    private void matchingProcess(MatchingOrder order, String correlationId) {
        // 남은 수량이 있는 동안 매칭 시도
        while (order.getQuantity().compareTo(BigDecimal.ZERO) > 0) {
            // Lua 스크립트 실행으로 매칭 처리 및 이벤트 발행
            MatchingResult result = tryMatchAndPublishEvent(order, correlationId);

            // 매칭 결과에 따른 후속 처리 및 Kafka 메시지 전송
            if (!result.isMatched()) {
                // 미체결 주문 처리
                saveUnmatchedOrder(order, correlationId);
                break;
            }

            // 매칭된 반대 주문 정보
            MatchingOrder oppositeOrder = result.getOppositeOrder();

            // 체결 정보 저장 및 Kafka 메시지 전송
            saveMatchOrder(order, oppositeOrder, result.getMatchedQuantity(), result.getMatchPrice(), correlationId);

            // 잔여 수량이 있을 경우 다음 매칭 준비
            BigDecimal remainingQuantity = result.getRemainingQuantity();

            if (remainingQuantity.compareTo(BigDecimal.ZERO) > 0) {
                // 내 주문이 남았을 때 - 다음 반복 준비
                order.setQuantity(remainingQuantity);
            } else {
                // 남은 수량이 없으면 루프 종료
                break;
            }
        }
    }

    /**
     * Lua 스크립트를 사용하여 주문 매칭 시도 및 이벤트 발행
     */
    private MatchingResult tryMatchAndPublishEvent(MatchingOrder order, String correlationId) {
        String oppositeOrderKey, currentOrderKey;
        if (OrderType.BUY.equals(order.getOrderType())) {
            oppositeOrderKey = SELL_ORDER_KEY + order.getTradingPair();
            currentOrderKey = BUY_ORDER_KEY + order.getTradingPair();
        } else {
            oppositeOrderKey = BUY_ORDER_KEY + order.getTradingPair();
            currentOrderKey = SELL_ORDER_KEY + order.getTradingPair();
        }

        // 타임스탬프가 없으면 현재 시간 설정
        if (order.getTimestamp() == null) {
            order.setTimestamp(System.currentTimeMillis());
        }

        // 주문 정보 직렬화 (타임스탬프 포함)
        String orderDetails = serializeOrder(order);

        // Lua 스크립트 실행
        List<Object> results = redisTemplate.execute(
                matchingScript,
                Arrays.asList(oppositeOrderKey, currentOrderKey),
                order.getOrderType().toString(),
                order.getPrice().toString(),
                order.getQuantity().toString(),
                orderDetails
        );

        // 복구를 위한 주문 접수 이벤트는 이미 발행되었으므로 삭제 가능
        eventPublisherV5.deleteEvent(correlationId, MatchingEventType.ORDER_RECEIVED);

        // 결과 파싱
        boolean matched = "true".equals(results.get(0));

        if (!matched) {
            // 미체결 상황이라면 복구를 위한 미체결 event 발행
            MatchingEvent unmatchedEvent = MatchingEvent.orderUnmatched(order, correlationId);
            eventPublisherV5.publish(unmatchedEvent);

            return new MatchingResult(false, null, null, BigDecimal.ZERO, order.getQuantity());
        }

        // 매칭 성공 시 결과 파싱
        String oppositeOrderDetails = (String) results.get(1);
        BigDecimal matchPrice = new BigDecimal((String) results.get(2));
        BigDecimal matchedQuantity = new BigDecimal((String) results.get(3));
        BigDecimal remainingQuantity = new BigDecimal((String) results.get(4));

        // 반대 주문 정보 파싱
        MatchingOrder oppositeOrder = deserializeOrder(
                oppositeOrderDetails,
                order.getOrderType() == OrderType.BUY ? OrderType.SELL : OrderType.BUY,
                order.getTradingPair(),
                matchPrice
        );

        // 체결 상황이라면 복구를 위한 체결 event 발행
        MatchingEvent matchedEvent = MatchingEvent.orderMatched(
                order, oppositeOrder, matchedQuantity, matchPrice, correlationId);
        eventPublisherV5.publish(matchedEvent);

        // 잔여 수량이 있을 경우 복구를 위한 잔여 체결 event 발행
        if (remainingQuantity.compareTo(BigDecimal.ZERO) > 0) {
            MatchingEvent remainingEvent = MatchingEvent.orderRemaining(
                    order, remainingQuantity, correlationId);
            eventPublisherV5.publish(remainingEvent);
        }

        return new MatchingResult(
                true,
                oppositeOrder,
                matchPrice,
                matchedQuantity,
                remainingQuantity
        );
    }

    /**
     * 미체결 주문 저장 및 Kafka 메시지 전송
     */
    private void saveUnmatchedOrder(MatchingOrder order, String correlationId) {
        // Kafka로 미체결 주문 데이터 전달
        messageSenderV5.sendUnmatchedOrderMessage(order, correlationId);

        // 미체결 이벤트 처리 완료로 표시 (이벤트 삭제)
        eventPublisherV5.deleteEvent(correlationId, MatchingEventType.ORDER_UNMATCHED);

        log.info("{} 미체결 : {}원 {}개 (주문ID: {}, 사용자ID: {})",
                order.getOrderType(), order.getPrice(),
                order.getQuantity(), order.getOrderId(), order.getUserId());
    }

    /**
     * 체결 결과를 기록하고 Kafka 메시지 전송
     */
    private void saveMatchOrder(MatchingOrder order, MatchingOrder oppositeOrder,
                                BigDecimal matchedQuantity, BigDecimal executionPrice, String correlationId) {

        // 매수/매도 주문 식별
        MatchingOrder buyOrder = OrderType.BUY.equals(order.getOrderType()) ? order : oppositeOrder;
        MatchingOrder sellOrder = OrderType.SELL.equals(order.getOrderType()) ? order : oppositeOrder;

        // Kafka로 체결 정보 전달
        messageSenderV5.sendMatchedOrderMessage(
                buyOrder, sellOrder, matchedQuantity, executionPrice, correlationId);

        // 체결 이벤트 처리 완료로 표시 (이벤트 삭제)
        eventPublisherV5.deleteEvent(correlationId, MatchingEventType.ORDER_MATCHED);

        // 잔여 수량 이벤트가 있으면 처리 (삭제하지 않고 다음 매칭 단계에서 사용)

        log.info("BUY 완전체결 : {}원 {}개 (주문ID: {}, 시간 구분 : {})",
                executionPrice, matchedQuantity,
                buyOrder.getOrderId(), buyOrder.getTimestamp());

        log.info("SELL 완전체결 : {}원 {}개 (주문ID: {}, 시간 구분 : {})",
                executionPrice, matchedQuantity,
                sellOrder.getOrderId(), sellOrder.getTimestamp());
    }

    /**
     * 주문을 직렬화
     * 형식: timestamp|quantity|userId|orderId
     */
    private String serializeOrder(MatchingOrder order) {
        String timeStr;
        if (order.getOrderType() == OrderType.BUY) {
            // 반전된 타임스탬프 사용
            timeStr = String.format("%013d", 9999999999999L - order.getTimestamp());
        } else {
            // 일반 타임스탬프 사용
            timeStr = String.format("%013d", order.getTimestamp());
        }

        return timeStr + "|" +
                order.getQuantity() + "|" +
                order.getUserId() + "|" +
                order.getOrderId();
    }

    /**
     * 직렬화된 주문을 역직렬화
     * 형식: timestamp|quantity|userId|orderId
     */
    private MatchingOrder deserializeOrder(String orderValue, OrderType orderType,
                                           String tradingPair, BigDecimal price) {
        String[] parts = orderValue.split("\\|");

        return new MatchingOrder(
                tradingPair,
                orderType,
                price,
                Long.parseLong(parts[0]),   //timestamp
                new BigDecimal(parts[1]),   // quantity
                UUID.fromString(parts[2]),  // userId
                UUID.fromString(parts[3])   // orderId
        );
    }

    /**
     * 매칭 결과를 저장하는 내부 클래스
     */
    @Getter
    private static class MatchingResult {
        private final boolean matched;
        private final MatchingOrder oppositeOrder;
        private final BigDecimal matchPrice;
        private final BigDecimal matchedQuantity;
        private final BigDecimal remainingQuantity;

        public MatchingResult(boolean matched, MatchingOrder oppositeOrder, BigDecimal matchPrice,
                              BigDecimal matchedQuantity, BigDecimal remainingQuantity) {
            this.matched = matched;
            this.oppositeOrder = oppositeOrder;
            this.matchPrice = matchPrice;
            this.matchedQuantity = matchedQuantity;
            this.remainingQuantity = remainingQuantity;
        }
    }

    /**
     * 주문 매칭 프로세스에서 사용하는 내부 DTO 클래스
     */
    @Setter
    @Getter
    @AllArgsConstructor
    public static class MatchingOrder {
        private final String tradingPair;
        private OrderType orderType;
        private BigDecimal price;
        private Long timestamp;
        private BigDecimal quantity;
        private UUID userId;
        private UUID orderId;

        public MatchingOrder(String tradingPair, OrderType orderType, BigDecimal price,
                             BigDecimal quantity, UUID userId, UUID orderId) {
            this.tradingPair = tradingPair;
            this.orderType = orderType;
            this.price = price;
            this.timestamp = null;
            this.quantity = quantity;
            this.userId = userId;
            this.orderId = orderId;
        }

        public static MatchingOrder fromCommand(CreateMatchingCommand command) {
            return new MatchingOrder(
                    command.tradingPair(),
                    command.orderType(),
                    command.price(),
                    command.quantity(),
                    command.userId(),
                    command.orderId()
            );
        }
    }
}