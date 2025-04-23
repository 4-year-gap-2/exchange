package com.exchange.matching.domain.service;

import com.exchange.matching.application.command.CreateMatchingCommand;
import com.exchange.matching.application.dto.enums.OrderType;
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
public class MatchingServiceV4 implements MatchingService {

    private static final String SELL_ORDER_KEY = "v4:orders:sell:";
    private static final String BUY_ORDER_KEY = "v4:orders:buy:";

    private final RedisTemplate<String, String> redisTemplate;
    private final RedisScript<List<Object>> matchingScript;

    public MatchingServiceV4(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;

        // Lua 스크립트 로드
        DefaultRedisScript<List<Object>> script = new DefaultRedisScript<>();
        script.setResultType((Class) List.class);
        try {
            ClassPathResource resource = new ClassPathResource("scripts/matching.lua");
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

        // 남은 수량이 있는 동안 매칭 시도
        while (order.getQuantity().compareTo(BigDecimal.ZERO) > 0) {

            // Lua 스크립트 실행으로 매칭 처리
            MatchingResult result = tryMatch(order);

            // 매칭 실패 시 미체결 주문 저장 후 종료
            if (!result.isMatched()) {
                saveUnmatchedOrder(order);
                break;
            }

            // 매칭된 반대 주문 정보
            MatchingOrder oppositeOrder = result.getOppositeOrder();

            // 체결 정보 저장
            saveMatchOrder(order, oppositeOrder, result.getMatchedQuantity(), result.getMatchPrice());

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
     * Lua 스크립트를 사용하여 주문 매칭 시도
     */
    private MatchingResult tryMatch(MatchingOrder order) {
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

        // 결과 파싱
        boolean matched = "true".equals(results.get(0));

        // 매칭 실패 시 빠른 종료
        if (!matched) {
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

        return new MatchingResult(
                true,
                oppositeOrder,
                matchPrice,
                matchedQuantity,
                remainingQuantity
        );
    }

    /**
     * 미체결 주문 저장
     */
    private void saveUnmatchedOrder(MatchingOrder order) {
        // kafka로 체결 서버로 데이터 전달 필요

        log.info("{} 미체결 : {}원 {}개 (주문ID: {}, 사용자ID: {})",
                order.getOrderType(), order.getPrice(),
                order.getQuantity(), order.getOrderId(), order.getUserId());
    }

    /**
     * 체결 결과를 기록
     */
    private void saveMatchOrder(MatchingOrder order, MatchingOrder oppositeOrder,
                             BigDecimal matchedQuantity, BigDecimal executionPrice) {
        // 체결 내역 이벤트 발행

        // 매수/매도 주문 식별
        MatchingOrder buyOrder = OrderType.BUY.equals(order.getOrderType()) ? order : oppositeOrder;
        MatchingOrder sellOrder = OrderType.SELL.equals(order.getOrderType()) ? order : oppositeOrder;

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
     * 형식: quantity|userId|orderId
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
                    UUID.randomUUID()
            );
        }
    }
}