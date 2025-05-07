package com.exchange.order.application.command;

import com.exchange.order.application.result.FindOrderResult;
import com.exchange.order.infrastructure.dto.KafkaOrderCancelEvent;
import com.exchange.order.infrastructure.dto.KafkaUserBalanceDecreaseEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderCommandService {
    private final KafkaTemplate<String, KafkaUserBalanceDecreaseEvent> DecreasekafkaTemplate;
    private final KafkaTemplate<String, KafkaOrderCancelEvent> CancelkafkaTemplate;
    private static final String TOPIC_USER_BALANCE_DECREASE = "order-to-user.execute-decrease-balance";
    private static final String TOPIC_ORDER_CANCEL_DECREASE = "order-to-matching.execute-order-cancel";

    public FindOrderResult createOrder(CreateOrderCommand command) {
        KafkaUserBalanceDecreaseEvent kafkaUserBalanceDecreaseEvent = KafkaUserBalanceDecreaseEvent.fromCommand(command);

        DecreasekafkaTemplate.send(TOPIC_USER_BALANCE_DECREASE, kafkaUserBalanceDecreaseEvent);
//        // 메시지 전송 실패 시 로그 추가 (이후에 추가하기)
//        kafkaTemplate.send(TOPIC_USER_BALANCE_DECREASE, kafkaUserBalanceDecreaseEvent)
//                .addCallback(
//                        result -> {},
//                        ex -> {/* 오류 처리 로직 */}
//                );
        return FindOrderResult.fromResult(kafkaUserBalanceDecreaseEvent);
    }

    public FindOrderResult cancelOrder(UUID userId, UUID orderId) {
        //1. 미체결 주문인지 1차 확인 -> 체결이 동시에 실행되었다면 주문 취소 실패

        //2. 매칭 서버에 주문 취소 이벤트 를 발행
        KafkaOrderCancelEvent kafkaOrderCancelEvent = new KafkaOrderCancelEvent(userId,orderId);
        CancelkafkaTemplate.send(TOPIC_ORDER_CANCEL_DECREASE, kafkaOrderCancelEvent);
        //3. 주문 취소 진행중 처리 완료
        return null;
    }
}
