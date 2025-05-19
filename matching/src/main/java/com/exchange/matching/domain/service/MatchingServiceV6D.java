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
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class MatchingServiceV6D implements MatchingService {

    private static final String SELL_ORDER_KEY = "v6d:orders:sell:";
    private static final String BUY_ORDER_KEY = "v6d:orders:buy:";
    private static final String MATCH_STREAM_KEY = "v6d:stream:matches";
    private static final String UNMATCH_STREAM_KEY = "v6d:stream:unmatched";
    private static final String PARTIAL_MATCHED_STREAM_KEY = "v6d:stream:partialMatched";
    private static final String IDEMPOTENCY_KEY = "v6d:idempotency:orders";
    private static final String ORDERBOOK_BIDS_KEY = "v6d:orderbook:%s:bids";
    private static final String ORDERBOOK_ASKS_KEY = "v6d:orderbook:%s:asks";
    private static final String COLD_DATA_REQUEST_STREAM_KEY = "v6d:stream:cold_data_request";
    private static final String COLD_DATA_STATUS_KEY = "v6d:cold_data_status:%s";
    private static final String PENDING_ORDERS_KEY = "v6d:pending_orders";
    private static final String CLOSING_PRICE_KEY = "market:closing_price:%s";
    
    @Value("${matching.price-diff-threshold:0.3}")
    private double priceDiffThreshold; // 기본값 30%

    private final RedisTemplate<String, String> redisTemplate;
    private final RedisScript<List<Object>> matchingScript;

    @Override
    public MatchingVersion getVersion() {
        return MatchingVersion.V6D;
    }

    public MatchingServiceV6D(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;

        // Lua 스크립트 로드
        DefaultRedisScript<List<Object>> script = new DefaultRedisScript<>();
        script.setResultType((Class) List.class);
        try {
            ClassPathResource resource = new ClassPathResource("scripts/matchingV6D.lua");
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
                matchingOrder.getQuantity(), matchingOrder.getUserId());

        matchingProcess(matchingOrder);
    }

    /**
     * 주문 매칭 프로세스 시작
     */
    private void matchingProcess(MatchingOrder order) {
        String tradingPair = order.getTradingPair();

        String oppositeOrderKey, currentOrderKey;

        if (OrderType.BUY.equals(order.getOrderType())) {
            oppositeOrderKey = SELL_ORDER_KEY + order.getTradingPair();
            currentOrderKey = BUY_ORDER_KEY + order.getTradingPair();
        } else {
            oppositeOrderKey = BUY_ORDER_KEY + order.getTradingPair();
            currentOrderKey = SELL_ORDER_KEY + order.getTradingPair();
        }

        // 호가 리스트 키 생성
        String bidOrderbookKey = String.format(ORDERBOOK_BIDS_KEY, tradingPair);
        String askOrderbookKey = String.format(ORDERBOOK_ASKS_KEY, tradingPair);

        // 콜드 데이터 관련 키 생성
        String coldDataStatusKey = String.format(COLD_DATA_STATUS_KEY, tradingPair);

        // 종가 데이터 관련 키 생성
        String closingPriceKey = String.format(CLOSING_PRICE_KEY, tradingPair);
        
        // 타임스탬프가 없으면 현재 시간 설정
        if (order.getTimestamp() == null) {
            order.setTimestamp(System.currentTimeMillis());
        }

        // 주문 정보 직렬화 (타임스탬프 포함)
        String orderDetails = serializeOrder(order);

        // 부분 체결을 위한 새 ID 생성
        String partialOrderId = UUID.randomUUID().toString();


        // Lua 스크립트 실행
        redisTemplate.execute(
                matchingScript,
                Arrays.asList(
                        oppositeOrderKey,              // KEYS[1]
                        currentOrderKey,               // KEYS[2]
                        MATCH_STREAM_KEY,              // KEYS[3]
                        UNMATCH_STREAM_KEY,            // KEYS[4]
                        PARTIAL_MATCHED_STREAM_KEY,    // KEYS[5]
                        IDEMPOTENCY_KEY,               // KEYS[6]
                        bidOrderbookKey,               // KEYS[7]
                        askOrderbookKey,               // KEYS[8]
                        COLD_DATA_REQUEST_STREAM_KEY,  // KEYS[9]
                        coldDataStatusKey,             // KEYS[10]
                        PENDING_ORDERS_KEY,            // KEYS[11]
                        closingPriceKey                // KEYS[12]
                ),
                order.getOrderType().toString(),  // ARGV[1]
                order.getPrice().toString(),      // ARGV[2]
                order.getQuantity().toString(),   // ARGV[3]
                orderDetails,                     // ARGV[4]
                order.getTradingPair(),           // ARGV[5]
                order.getOrderId().toString(),    // ARGV[6]
                partialOrderId,                   // ARGV[7]
                String.valueOf(priceDiffThreshold) // ARGV[8]
        );
    }

    /**
     * 주문 직렬화
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