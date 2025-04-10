package com.exchange.matching.domain.service;

import com.exchange.matching.application.dto.enums.OrderType;
import com.exchange.matching.infrastructure.dto.KafkaMatchingEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

@Service
@Slf4j
public class MatchingServiceV3 implements MatchingService {

    private static final String SELL_ORDER_KEY = "orders:sell:";
    private static final String BUY_ORDER_KEY = "orders:buy:";

    private final RedisTemplate<String, String> redisTemplate;

    public MatchingServiceV3(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void matchOrders(KafkaMatchingEvent event) {
        MatchingOrder matchingOrder = MatchingOrder.fromEvent(event);
        matchingProcess(matchingOrder);
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
                    break;
                }
            } else {
                // 매수 주문 높은가격 pop
                oppositeOrder = findHighestBuyOrder(order.getTradingPair());

                // 매수 주문이 없거나 가격이 맞지 않으면 미체결 처리 후 종료
                if (oppositeOrder == null ||
                    oppositeOrder.getPrice().compareTo(order.getPrice()) < 0) {
                    saveUnmatchedOrder(order);
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
        BigDecimal remainingOrderQuantity = order.getQuantity().subtract(matchedQuantity);
        BigDecimal remainingOppositeQuantity = oppositeOrder.getQuantity().subtract(matchedQuantity);

        //실제 체결 되는 가격은 반대 주문 가격 설정
        BigDecimal executionPrice = oppositeOrder.getPrice();

        // 체결 기록 및 이벤트 발행
        recordMatch(order, oppositeOrder, matchedQuantity, executionPrice);

        // 주문 수량 업데이트
        order.setQuantity(remainingOrderQuantity);

        // 반대 주문에 잔여 수량이 있으면 다시 저장
        if (remainingOppositeQuantity.compareTo(BigDecimal.ZERO) > 0) {
            order.setTradingPair(oppositeOrder.getTradingPair());
            order.setOrderType(oppositeOrder.getOrderType());
            order.setOrderId(oppositeOrder.getOrderId());
            order.setPrice(oppositeOrder.getPrice());
            order.setUserId(oppositeOrder.getUserId());
        }
    }

    /**
     * 최저가 매도 주문 pop
     */
    private MatchingOrder findLowestSellOrder(String tradingPair) {
        String sellOrderKey = SELL_ORDER_KEY + tradingPair;

        // ZPOPMIN 사용하여 최저가 매도 주문을 원자적으로 가져오고 제거
        Set<ZSetOperations.TypedTuple<String>> lowestSellOrders = redisTemplate.opsForZSet()
                .popMin(sellOrderKey, 1);

        if (lowestSellOrders == null || lowestSellOrders.isEmpty()) {
            return null;
        }

        ZSetOperations.TypedTuple<String> lowestSellOrder = lowestSellOrders.iterator().next();

        // 가격 정보를 score에서 가져와서 MatchingOrder 생성
        return deserializeOrder(
                lowestSellOrder.getValue(),
                OrderType.SELL,
                tradingPair,
                BigDecimal.valueOf(lowestSellOrder.getScore())
        );
    }

    /**
     * 최고가 매수 주문 pop
     */
    private MatchingOrder findHighestBuyOrder(String tradingPair) {
        String buyOrderKey = BUY_ORDER_KEY + tradingPair;

        Set<ZSetOperations.TypedTuple<String>> highestBuyOrders = redisTemplate.opsForZSet()
                .popMax(buyOrderKey, 1);

        if (highestBuyOrders == null || highestBuyOrders.isEmpty()) {
            return null;
        }

        ZSetOperations.TypedTuple<String> highestBuyOrder = highestBuyOrders.iterator().next();
        // 가격 정보를 score에서 가져와서 MatchingOrder 생성
        return deserializeOrder(
                highestBuyOrder.getValue(),
                OrderType.BUY,
                tradingPair,
                BigDecimal.valueOf(highestBuyOrder.getScore())
        );
    }

    /**
     * 체결 결과를 기록합니다.
     */
    private void recordMatch(MatchingOrder order, MatchingOrder oppositeOrder,
                             BigDecimal matchedQuantity, BigDecimal executionPrice) {
        // 실제 구현에서는 여기서 체결 결과를 DB에 저장하거나 이벤트로 발행할 수 있습니다.
        // 매수/매도 주문 식별
        MatchingOrder buyOrder = OrderType.BUY.equals(order.getOrderType()) ? order : oppositeOrder;
        MatchingOrder sellOrder = OrderType.SELL.equals(order.getOrderType()) ? order : oppositeOrder;

        System.out.println("주문 체결: " +
                "매수자=" + buyOrder.getUserId() + ", " +
                "매도자=" + sellOrder.getUserId() + ", " +
                "거래쌍=" + order.getTradingPair() + ", " +
                "가격=" + executionPrice + ", " +
                "수량=" + matchedQuantity);
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
        private String tradingPair;
        private OrderType orderType;
        private BigDecimal price;
        private BigDecimal quantity;
        private UUID userId;
        private UUID orderId;

        public static MatchingOrder fromEvent(KafkaMatchingEvent event) {
            return new MatchingOrder(
                    event.tradingPair(),
                    event.orderType(),
                    event.price(),
                    event.quantity(),
                    event.userId(),
                    event.orderId()
            );
        }
    }
}