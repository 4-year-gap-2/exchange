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

    private static final String SELL_ORDER_KEY = "mjy:orders:sell:";
    private static final String BUY_ORDER_KEY = "mjy:orders:buy:";

    private final RedisTemplate<String, String> redisTemplate;
    private final RedisScript<List<Object>> matchingScript;

    public MatchingServiceV4(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;

        // Lua 스크립트 로드
        DefaultRedisScript<List<Object>> script = new DefaultRedisScript<>();
        // 원시 타입(raw type)을 사용하여 문제 해결
        script.setResultType((Class) List.class);
        try {
            // 스크립트 파일 위치는 실제 환경에 맞게 조정 (resources/scripts/matching.lua)
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
                // 새로운 주문 유형 정보 활용
                String remainingOrderType = result.getRemainingOrderType();

                // 남은 주문이 현재 주문의 타입과 다르면, 새로운 주문 객체로 전환
                if (!order.getOrderType().toString().equals(remainingOrderType)) {
                    log.info("남은 주문 타입 변경: {} -> {}", order.getOrderType(), remainingOrderType);

                    // 남은 주문 상세 정보 파싱
                    String[] parts = result.getRemainingOrderDetails().split("\\|");
                    UUID userId = UUID.fromString(parts[1]);
                    UUID orderId = UUID.fromString(parts[2]);

                    // 새로운 주문 객체 생성
                    order = new MatchingOrder(
                            order.getTradingPair(),
                            OrderType.valueOf(remainingOrderType),
                            result.getMatchPrice(),  // 체결 가격 사용
                            remainingQuantity,
                            userId,
                            orderId
                    );
                } else {
                    // 동일한 타입의 주문이 남은 경우 수량만 업데이트
                    order.setQuantity(remainingQuantity);
                }
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
            oppositeOrderKey = SELL_ORDER_KEY + order.getTradingPair(); // 매수 주문이므로 매도 주문을 조회
            currentOrderKey = BUY_ORDER_KEY + order.getTradingPair();
        } else {
            oppositeOrderKey = BUY_ORDER_KEY + order.getTradingPair();  // 매도 주문이므로 매수 주문을 조회
            currentOrderKey = SELL_ORDER_KEY + order.getTradingPair();
        }

        // 5자리 시간 fragment 생성
        long rawTime = System.currentTimeMillis() % 100000;
        // 매수 주문의 경우 시간을 반전시켜 우선순위 조정
        int timePart = (order.getOrderType() == OrderType.BUY) ? (100000 - (int) rawTime) : (int) rawTime;
        String timeStr = String.format("%05d", timePart); // 항상 5자리

        // 주문 정보 직렬화
        String orderDetails = serializeOrder(order);

        // price + 시간 문자열 이어붙여서 score 생성
        double score = Double.parseDouble(order.getPrice() + timeStr);

        // Lua 스크립트 실행
        List<Object> results = redisTemplate.execute(
                matchingScript,
                Arrays.asList(oppositeOrderKey, currentOrderKey),
                order.getOrderType().toString(),
                order.getPrice().toString(),
                order.getQuantity().toString(),
                orderDetails,
                String.valueOf(score)
        );

        // 스크립트 실행 결과 로깅
        log.info("Lua 스크립트 결과: {}", results);

        // 스크립트 실행 결과 파싱 - 문자열로 받아서 처리
        boolean matched = "true".equals(results.get(0));

        // 미체결 주문 처리
        if (!matched) {
            return new MatchingResult(false, null, null, BigDecimal.ZERO, order.getQuantity(), "", "");
        }

        String oppositeOrderDetails = (String) results.get(1);
        double oppositeScore = "".equals(results.get(2)) ? 0.0 : Double.parseDouble((String)results.get(2));
        BigDecimal matchPrice = "".equals(results.get(3)) ? BigDecimal.ZERO : new BigDecimal((String)results.get(3));
        BigDecimal matchedQuantity = "".equals(results.get(4)) ? BigDecimal.ZERO : new BigDecimal((String)results.get(4));
        BigDecimal remainingQuantity = "".equals(results.get(5)) ? BigDecimal.ZERO : new BigDecimal((String)results.get(5));
        String remainingOrderType = (String) results.get(6);
        String remainingOrderDetails = (String) results.get(7);

        log.debug("매칭 결과: 체결가격={}, 체결수량={}, 남은수량={}, 남은주문타입={}",
                matchPrice, matchedQuantity, remainingQuantity, remainingOrderType);

        // 시간 부분 추출 (나머지 계산)
        BigDecimal scoreAsBigDecimal = BigDecimal.valueOf(oppositeScore);
        BigDecimal divisor = new BigDecimal(100000);
        BigDecimal timePart2 = scoreAsBigDecimal.remainder(divisor);
        String timeStr2 = String.format("%05d", timePart2.intValue());

        // 반대 주문 정보 역직렬화
        MatchingOrder oppositeOrder = deserializeOrder(
                oppositeOrderDetails,
                order.getOrderType() == OrderType.BUY ? OrderType.SELL : OrderType.BUY,
                order.getTradingPair(),
                matchPrice
        );

        // 시간 정보 저장
        oppositeOrder.setOriginalTimeStr(timeStr2);

        return new MatchingResult(
                true,
                oppositeOrder,
                matchPrice,
                matchedQuantity,
                remainingQuantity,
                remainingOrderType,
                remainingOrderDetails
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
        // kafka로 체결 서버로 데이터 전달 필요

        // 매수/매도 주문 식별
        MatchingOrder buyOrder = OrderType.BUY.equals(order.getOrderType()) ? order : oppositeOrder;
        MatchingOrder sellOrder = OrderType.SELL.equals(order.getOrderType()) ? order : oppositeOrder;

        log.info("BUY 완전체결 : {}원 {}개 (주문ID: {}, 시간 구분 : {})",
                executionPrice, matchedQuantity,
                buyOrder.getOrderId(), buyOrder.getOriginalTimeStr());

        log.info("SELL 완전체결 : {}원 {}개 (주문ID: {}, 시간 구분 : {})",
                executionPrice, matchedQuantity,
                sellOrder.getOrderId(), sellOrder.getOriginalTimeStr());
    }

    /**
     * 주문을 직렬화
     * 형식: quantity|userId|orderId
     */
    private String serializeOrder(MatchingOrder order) {
        return order.getQuantity() + "|" +
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
                new BigDecimal(parts[0]),  // quantity
                UUID.fromString(parts[1]),  // userId
                UUID.fromString(parts[2])   // orderId
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
        private final String remainingOrderType;
        private final String remainingOrderDetails;

        public MatchingResult(boolean matched, MatchingOrder oppositeOrder, BigDecimal matchPrice,
                              BigDecimal matchedQuantity, BigDecimal remainingQuantity,
                              String remainingOrderType, String remainingOrderDetails) {
            this.matched = matched;
            this.oppositeOrder = oppositeOrder;
            this.matchPrice = matchPrice;
            this.matchedQuantity = matchedQuantity;
            this.remainingQuantity = remainingQuantity;
            this.remainingOrderType = remainingOrderType;
            this.remainingOrderDetails = remainingOrderDetails;
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
        private BigDecimal quantity;
        private UUID userId;
        private UUID orderId;
        private String originalTimeStr;

        public MatchingOrder(String tradingPair, OrderType orderType, BigDecimal price,
                             BigDecimal quantity, UUID userId, UUID orderId) {
            this.tradingPair = tradingPair;
            this.orderType = orderType;
            this.price = price;
            this.quantity = quantity;
            this.userId = userId;
            this.orderId = orderId;
            this.originalTimeStr = null;
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