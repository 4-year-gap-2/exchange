package com.exchange.order.application.command;

import com.exchange.order.application.result.FindOrderResult;
import com.exchange.order.infrastructure.dto.KafkaUserBalanceDecreaseEvent;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderCommandService {
    private final KafkaTemplate<String, KafkaUserBalanceDecreaseEvent> kafkaTemplate;

    public FindOrderResult createOrder(CreateOrderCommand command) {
        KafkaUserBalanceDecreaseEvent kafkaUserBalanceDecreaseEvent = KafkaUserBalanceDecreaseEvent.fromCommand(command);
        kafkaTemplate.send("order-to-user.excute-decrease-balance", kafkaUserBalanceDecreaseEvent);
        return FindOrderResult.fromResult(kafkaUserBalanceDecreaseEvent);
    }
}
