package com.exchange.order_completed.infrastructure.external;

import com.exchange.order_completed.infrastructure.dto.KafkaBalanceIncreaseEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaEventPublisher{

    private final KafkaTemplate<String, KafkaBalanceIncreaseEvent> kafkaTemplate;
    private static final String TOPIC = "order_completed-to-user.execute-increase-balance";

    public void publishMessage(KafkaBalanceIncreaseEvent event) {
        kafkaTemplate.send(TOPIC, event);
    }
}
