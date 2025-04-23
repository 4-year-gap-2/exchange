package com.exchange.order.application.command;

import com.exchange.order.application.result.FindOrderResult;
import com.exchange.order.infrastructure.dto.KafkaUserBalanceDecreaseEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderCommandService {
    private final KafkaTemplate<String, KafkaUserBalanceDecreaseEvent> kafkaTemplate;
    private static final String TOPIC_USER_BALANCE_DECREASE = "order-to-user.execute-decrease-balance";

    public FindOrderResult createOrder(CreateOrderCommand command) {
        KafkaUserBalanceDecreaseEvent kafkaUserBalanceDecreaseEvent = KafkaUserBalanceDecreaseEvent.fromCommand(command);

        kafkaTemplate.send(TOPIC_USER_BALANCE_DECREASE, kafkaUserBalanceDecreaseEvent);
//        // 메시지 전송 실패 시 로그 추가 (이후에 추가하기)
//        kafkaTemplate.send(TOPIC_USER_BALANCE_DECREASE, kafkaUserBalanceDecreaseEvent)
//                .addCallback(
//                        result -> {},
//                        ex -> {/* 오류 처리 로직 */}
//                );
        return FindOrderResult.fromResult(kafkaUserBalanceDecreaseEvent);
    }
}
