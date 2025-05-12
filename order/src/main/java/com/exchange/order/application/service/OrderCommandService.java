package com.exchange.order.application.service;

import com.exchange.order.application.command.CreateOrderCommand;
import com.exchange.order.application.enums.OrderType;
import com.exchange.order.application.result.FindCancelResult;
import com.exchange.order.application.result.FindOrderResult;
import com.exchange.order.infrastructure.dto.KafkaOrderCancelEvent;
import com.exchange.order.infrastructure.dto.KafkaUserBalanceDecreaseEvent;
import com.exchange.order.presentation.request.CancelOrderRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
@Slf4j
public class OrderCommandService {

    private final KafkaTemplate<String, KafkaUserBalanceDecreaseEvent> DecreasekafkaTemplate;
    private final KafkaTemplate<String, KafkaOrderCancelEvent> CancelkafkaTemplate;
    private static final String TOPIC_USER_BALANCE_DECREASE = "order-to-user.execute-decrease-balance";
    private static final String SELL_ORDER_KEY = "v6:orders:sell:";
    private static final String BUY_ORDER_KEY = "v6:orders:buy:";
    private static final String CANCEL_STREAM_KEY = "stream:cancel"; // lua 스크립트 로드

    private final StringRedisTemplate redisTemplate;
    private final RedisScript<String> removeOrderScript;

    //주문 생성
    public FindOrderResult createOrder(CreateOrderCommand command) {
        KafkaUserBalanceDecreaseEvent kafkaUserBalanceDecreaseEvent = KafkaUserBalanceDecreaseEvent.fromCommand(command);

        DecreasekafkaTemplate.send(TOPIC_USER_BALANCE_DECREASE, kafkaUserBalanceDecreaseEvent);
        return FindOrderResult.fromResult(kafkaUserBalanceDecreaseEvent);
    }

    //주문 취소
    public FindCancelResult cancelOrder(UUID userId, CancelOrderRequest request) {
        log.info("removeOrderScript Bean 타입: {}", removeOrderScript.getResultType());
        log.info("redisTemplate Bean 타입: {}", redisTemplate.getClass());
        // 1.유저가 맞 주문 취소한 유저가 해당 는지 확인
        if (!userId.equals(UUID.fromString(request.getUserId()))) {
            throw new IllegalArgumentException("주문 취소 권한이 없습니다.");
        }

        // 2. sorted set 키 설정
        String tradingPair = request.getTradingPair();
        String orderType = request.getOrderType();
        String zsetKey;
        if ("BUY".equals(request.getOrderType())) {
            zsetKey = BUY_ORDER_KEY + request.getTradingPair();
        } else {
            zsetKey = SELL_ORDER_KEY + request.getTradingPair();
        }        String value = serializeOrderForCancel(
                request.getTimestamp(),
                request.getQuantity(),
                request.getUserId(),
                request.getOrderId()
        );
        System.out.println(zsetKey);

        // 3. 루아스크립트로 미체결 주문 제거하기
        List<String> keys = Arrays.asList(zsetKey , CANCEL_STREAM_KEY);
        log.info("삭제 시도 value: '{}', length: {}", value, value.length());
        try {
            // 예외 발생 가능성이 있는 코드
            String result = redisTemplate.execute(removeOrderScript, keys, value, tradingPair, orderType);

            if (result != null && !result.isEmpty()) {
                log.info("삭제 성공: {}", result);
            } else {
                log.info("삭제 실패");
            }
        } catch (Exception e) {
            e.printStackTrace(); // 콘솔에 스택트레이스 출력
            log.error("예외 발생!", e); // 로그 파일에도 스택트레이스 출력
        }

        //3. 주문 취소 진행중 처리 완료
        return FindCancelResult.fromResult(request);
    }

    private String serializeOrderForCancel(String timestamp, String quantity, String userId, String orderId)
    {
        String timeStr = String.format("%013d", Long.parseLong(timestamp));
        return timeStr + "|" + quantity + "|" + userId + "|" + orderId;
    }
}
