package com.exchange.matching.application.service;

import com.exchange.matching.application.command.CreateMatchingCommand;
import com.exchange.matching.application.dto.enums.OrderType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

@Service
@Slf4j
public class MatchingServiceV3 implements MatchingService {

    private static final String SELL_ORDER_KEY = "mjy:orders:sell:";
    private static final String BUY_ORDER_KEY = "mjy:orders:buy:";

    private final RedisTemplate<String, String> redisTemplate;

    public MatchingServiceV3(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public String matchOrders(CreateMatchingCommand command) {
        MatchingOrder matchingOrder = MatchingOrder.fromCommand(command);
        matchingProcess(matchingOrder);
        return "good";
    }

    /**
     * 주문 매칭 프로세스 시작
     */
    private void matchingProcess(MatchingOrder order) {
        // 남은 수량이 있는 동안 매칭 시도
        while (order.getQuantity().compareTo(BigDecimal.ZERO) > 0) {
            MatchingOrder oppositeOrder;
            if (OrderType.BUY.equals(order.getOrderType())) {
                // 매도 주문 낮은가격 pop
                oppositeOrder = findLowestSellOrder(order.getTradingPair());

                // 매도 주문이 없거나 가격이 맞지 않으면 미체결 처리 후 종료
                if (oppositeOrder == null ||
                        oppositeOrder.getPrice().compareTo(order.getPrice()) > 0) {
                    saveUnmatchedOrder(order);
                    if (oppositeOrder != null) {
                        saveRestoreOppositeOrder(oppositeOrder);
                    }
                    break;
                }
            } else {
                // 매수 주문 높은가격 pop
                oppositeOrder = findHighestBuyOrder(order.getTradingPair());

                // 매수 주문이 없거나 가격이 맞지 않으면 미체결 처리 후 종료
                if (oppositeOrder == null ||
                        oppositeOrder.getPrice().compareTo(order.getPrice()) < 0) {
                    saveUnmatchedOrder(order);
                    if (oppositeOrder != null) {
                        saveRestoreOppositeOrder(oppositeOrder);
                    }
                    break;
                }
            }

            // 매칭 수행
            processMatch(order, oppositeOrder);
        }
    }

    /**
     * 주문 매칭 처리
     */
    private void processMatch(MatchingOrder order, MatchingOrder oppositeOrder) {
        // 매칭 가능한 수량 계산
        BigDecimal matchedQuantity = order.getQuantity().min(oppositeOrder.getQuantity());
        BigDecimal remainingOrderQuantity = order.getQuantity().subtract(oppositeOrder.getQuantity());

        //실제 체결 되는 가격은 반대 주문 가격 설정
        BigDecimal executionPrice = oppositeOrder.getPrice();

        // 체결 기록 및 이벤트 발행
        recordMatch(order, oppositeOrder, matchedQuantity, executionPrice);

        if (remainingOrderQuantity.compareTo(BigDecimal.ZERO) >= 0) {
            // 주문에 잔여 수량이 있으면 수량 변경후 계속 진행
            order.setQuantity(remainingOrderQuantity);
        } else {
            // 반대 주문 수량 변경후 저장후 종료
            oppositeOrder.setQuantity(remainingOrderQuantity.abs());
            saveUnmatchedOrder(oppositeOrder);

            // 현재 주문 수량을 0으로 설정하여 while 루프 종료 조건을 만듦
            order.setQuantity(BigDecimal.ZERO);
        }
    }

    /**
     * 최저가 매도 주문 pop
     */
    private MatchingOrder findLowestSellOrder(String tradingPair) {
        String sellOrderKey = SELL_ORDER_KEY + tradingPair;

        // ZPOPMIN 사용하여 최저가 매도 주문을 가져오고 제거
        Set<ZSetOperations.TypedTuple<String>> lowestSellOrders = redisTemplate.opsForZSet()
                .popMin(sellOrderKey, 1);

        if (lowestSellOrders == null || lowestSellOrders.isEmpty()) {
            return null;
        }

        ZSetOperations.TypedTuple<String> lowestSellOrder = lowestSellOrders.iterator().next();
        double originalScore = lowestSellOrder.getScore();

        // double을 BigDecimal로 정확하게 변환
        BigDecimal scoreAsBigDecimal = BigDecimal.valueOf(originalScore);

        // 수학적 방법으로 분할 (문자열 변환 없이)
        BigDecimal divisor = new BigDecimal(100000);
        BigDecimal price = scoreAsBigDecimal.divideToIntegralValue(divisor);

        // 시간 부분 추출 (나머지 계산)
        BigDecimal timePart = scoreAsBigDecimal.remainder(divisor);
        String timeStr = String.format("%05d", timePart.intValue());

        // 가격 정보를 score에서 가져와서 MatchingOrder 생성
        MatchingOrder order = deserializeOrder(
                lowestSellOrder.getValue(),
                OrderType.SELL,
                tradingPair,
                price
        );

        // 다시 redis에 저장 위해 원래 시간 부분을 저장
        order.setOriginalTimeStr(timeStr);

        return order;
    }

    /**
     * 최고가 매수 주문 pop
     */
    private MatchingOrder findHighestBuyOrder(String tradingPair) {
        String buyOrderKey = BUY_ORDER_KEY + tradingPair;

        // ZPOPMAX 사용하여 최고가 매수 주문을 가져오고 제거
        Set<ZSetOperations.TypedTuple<String>> highestBuyOrders = redisTemplate.opsForZSet()
                .popMax(buyOrderKey, 1);

        if (highestBuyOrders == null || highestBuyOrders.isEmpty()) {
            return null;
        }

        ZSetOperations.TypedTuple<String> highestBuyOrder = highestBuyOrders.iterator().next();
        double originalScore = highestBuyOrder.getScore();

        // double을 BigDecimal로 정확하게 변환
        BigDecimal scoreAsBigDecimal = BigDecimal.valueOf(originalScore);

        // 수학적 방법으로 분할 (문자열 변환 없이)
        BigDecimal divisor = new BigDecimal(100000);
        BigDecimal price = scoreAsBigDecimal.divideToIntegralValue(divisor);

        // 시간 부분 추출 (나머지 계산)
        BigDecimal timePart = scoreAsBigDecimal.remainder(divisor);
        String timeStr = String.format("%05d", timePart.intValue());

        MatchingOrder order = deserializeOrder(
                highestBuyOrder.getValue(),
                OrderType.BUY,
                tradingPair,
                price
        );

        // 다시 redis에 저장 위해 원래 시간 부분을 저장
        order.setOriginalTimeStr(timeStr);

        return order;
    }

    /**
     * 체결 결과를 기록
     */
    private void recordMatch(MatchingOrder order, MatchingOrder oppositeOrder,
                             BigDecimal matchedQuantity, BigDecimal executionPrice) {
        // 실제 구현에서는 여기서 체결 결과를 DB에 저장하거나 이벤트로 발행
        // 매수/매도 주문 식별
        MatchingOrder buyOrder = OrderType.BUY.equals(order.getOrderType()) ? order : oppositeOrder;
        MatchingOrder sellOrder = OrderType.SELL.equals(order.getOrderType()) ? order : oppositeOrder;

        log.info("BUY 체결 : {}원 {}개 (주문ID: {}, 사용자ID: {}, 시간 구분 : {})",
                executionPrice, matchedQuantity,
                buyOrder.getOrderId(), buyOrder.getUserId(), buyOrder.getOriginalTimeStr());

        log.info("SELL 체결 : {}원 {}개 (주문ID: {}, 사용자ID: {}, 시간 구분 : {})",
                executionPrice, matchedQuantity,
                sellOrder.getOrderId(), sellOrder.getUserId(), sellOrder.getOriginalTimeStr());
    }

    /**
     * 미체결 주문 저장
     */
    private void saveUnmatchedOrder(MatchingOrder order) {
        String orderKey = OrderType.BUY.equals(order.getOrderType())
                ? BUY_ORDER_KEY + order.getTradingPair()
                : SELL_ORDER_KEY + order.getTradingPair();

        String orderDetails = serializeOrder(order);

        // 5자리 시간 fragment 생성
        long rawTime = System.currentTimeMillis() % 100000;
        // 같은 가격일때 더 오래된 시간을 비교한다 오래된 시간은 값이 낮은데 매도는 시간 값이 낮아도 정상 추출이 되지만
        // 매수는 더 높은 가격을 추출해야 하는데 시간이 낮으면 우선순위가 뒤로 밀려난다. 그래서 100000을 빼서 값이 뒤집히도록 처리한다.
        int timePart = (order.getOrderType() == OrderType.BUY) ? (100000 - (int) rawTime) : (int) rawTime;
        String timeStr = String.format("%05d", timePart); // 항상 5자리

        // price + 시간 문자열 이어붙여서 score 생성
        double score = Double.parseDouble(order.price + timeStr);

        redisTemplate.opsForZSet().add(orderKey, orderDetails, score);

        log.info("{} 미체결 : {}원 {}개 (주문ID: {}, 사용자ID: {})",
                order.getOrderType(), order.getPrice(),
                order.getQuantity(), order.getOrderId(), order.getUserId());
    }

    /**
     * 반대 주문 다시 저장
     */
    private void saveRestoreOppositeOrder(MatchingOrder order) {
        String orderKey = OrderType.BUY.equals(order.getOrderType())
                ? BUY_ORDER_KEY + order.getTradingPair()
                : SELL_ORDER_KEY + order.getTradingPair();

        String orderDetails = serializeOrder(order);

        String timeStr = order.getOriginalTimeStr();
        if (timeStr == null) {
            long rawTime = System.currentTimeMillis() % 100000;
            int timePart = (order.getOrderType() == OrderType.BUY) ? (100000 - (int) rawTime) : (int) rawTime;
            timeStr = String.format("%05d", timePart);
        }

        double score = Double.parseDouble(order.getPrice() + timeStr);

        redisTemplate.opsForZSet().add(orderKey, orderDetails, score);

        log.info("{} pop후 재삽입 : {}원 {}개 (주문ID: {}, 사용자ID: {})",
                order.getOrderType(), order.getPrice(),
                order.getQuantity(), order.getOrderId(), order.getUserId());
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
                price,  // Redis sorted set의 score 값
                new BigDecimal(parts[0]),  // quantity
                UUID.fromString(parts[1]),  // userId
                UUID.fromString(parts[2])   // orderId
        );
    }

    /**
     * 주문 매칭 프로세스에서 사용하는 내부 DTO 클래스
     * 매칭 수량을 변경할 수 있도록 mutable하게 설계
     */
    @Setter
    @Getter
    @AllArgsConstructor
    public static class MatchingOrder {
        private final String tradingPair;
        private final OrderType orderType;
        private final BigDecimal price;
        private BigDecimal quantity;
        private final UUID userId;
        private final UUID orderId;
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