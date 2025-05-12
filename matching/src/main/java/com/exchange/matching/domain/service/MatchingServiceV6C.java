package com.exchange.matching.domain.service;

import com.exchange.matching.application.command.CreateMatchingCommand;
import com.exchange.matching.application.enums.MatchingVersion;
import com.exchange.matching.application.enums.OrderType;
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

@Slf4j
@Service
public class MatchingServiceV6C implements MatchingService {

    private static final String SELL_ORDER_KEY = "v6c:orders:sell:";
    private static final String BUY_ORDER_KEY = "v6c:orders:buy:";
    private static final String MATCH_STREAM_KEY = "v6c:stream:matches";
    private static final String UNMATCH_STREAM_KEY = "v6c:stream:unmatched";
    private static final String PARTIAL_MATCHED_STREAM_KEY = "v6c:stream:partialMatched";
    private static final String IDEMPOTENCY_KEY = "v6c:idempotency:orders";

    private final RedisTemplate<String, String> redisTemplate;
    private final RedisScript<List<Object>> matchingScript;

    @Override
    public MatchingVersion getVersion() {
        return MatchingVersion.V6C;
    }

    public MatchingServiceV6C(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;

        // Lua 스크립트 로드
        DefaultRedisScript<List<Object>> script = new DefaultRedisScript<>();
        script.setResultType((Class) List.class);
        try {
            ClassPathResource resource = new ClassPathResource("scripts/matchingV6C.lua");
            String scriptText = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            script.setScriptText(scriptText);
        } catch (IOException e) {
            log.error("Lua 스크립트 로드 실패", e);
            throw new RuntimeException("Lua 스크립트 로드 실패", e);
        }
        this.matchingScript = script;
    }

    public void matchOrders(CreateMatchingCommand command) {
        MatchingOrder matchingOrder = MatchingOrder.fromCommand(command);

        log.info("{} 주문접수 : {}원 {}개 (주문ID: {})",
                matchingOrder.getOrderType(), matchingOrder.getPrice(),
                matchingOrder.getQuantity(), matchingOrder.getOrderId());

        matchingProcess(matchingOrder);
    }

    /**
     * 주문 매칭 프로세스 시작
     */
    private void matchingProcess(MatchingOrder order) {
        // 1. 기본 유효성 검증
        if (order.getPrice() == null
            || order.getOrderId() == null
            || order.getQuantity() == null
            || order.getTradingPair() == null) {
            throw new IllegalArgumentException("필수 주문 정보가 누락되었습니다.");
        }

        // 2. 주문 키 및 타임스탬프 준비
        String oppositeOrderKey, currentOrderKey;
        if (OrderType.BUY.equals(order.getOrderType())) {
            oppositeOrderKey = SELL_ORDER_KEY + order.getTradingPair();
            currentOrderKey = BUY_ORDER_KEY + order.getTradingPair();
        } else {
            oppositeOrderKey = BUY_ORDER_KEY + order.getTradingPair();
            currentOrderKey = SELL_ORDER_KEY + order.getTradingPair();
        }

        // 3. 타임스탬프 처리
        if (order.getTimestamp() == null) {
            order.setTimestamp(System.currentTimeMillis());
        }

        String orderId = order.getOrderId().toString();

        // 4. 타임스탬프 포맷팅
        String formattedTimestamp = formatTimestamp(order.getTimestamp(), order.getOrderType());

        // 5. 주문 실행 및 부분 체결 ID 생성
        String partialOrderId = UUID.randomUUID().toString();

        // Lua 스크립트 실행
        redisTemplate.execute(
                matchingScript,
                Arrays.asList(
                        oppositeOrderKey,
                        currentOrderKey,
                        MATCH_STREAM_KEY,
                        UNMATCH_STREAM_KEY,
                        PARTIAL_MATCHED_STREAM_KEY,
                        IDEMPOTENCY_KEY
                ),
                order.getOrderType().toString(),
                order.getPrice().toString(),
                order.getQuantity().toString(),
                formattedTimestamp,               // 포맷팅된 타임스탬프
                order.getUserId().toString(),     // 사용자 ID
                orderId,                          // 주문 ID
                partialOrderId,                   // 부분 체결 ID
                order.getTradingPair()            // 거래 쌍
        );
    }

    /**
     * 타임스탬프 포맷팅
     * 매수 주문의 경우 반전된 타임스탬프 사용
     */
    private String formatTimestamp(Long timestamp, OrderType orderType) {
        if (orderType == OrderType.BUY) {
            // 반전된 타임스탬프 사용
            return String.format("%013d", 9999999999999L - timestamp);
        } else {
            // 일반 타임스탬프 사용
            return String.format("%013d", timestamp);
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
//                    command.orderId()
                    UUID.randomUUID()
            );
        }
    }
}