package com.springcloud.user.infrastructure.external;


import com.springcloud.user.application.command.DecreaseBalanceCommand;
import com.springcloud.user.application.service.UserService;
import com.springcloud.user.infrastructure.dto.KafkaUserBalanceDecreaseEvent;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BalanceEventConsumer {

    private final UserService userService;


    @KafkaListener(
            topics = {"user-balance-Decrease"},
            groupId = "order-service",
            concurrency = "3"  // 3개의 스레드로 병렬 처리
    )
    public void increaseBalance(ConsumerRecord<String, KafkaUserBalanceDecreaseEvent> record) {

        DecreaseBalanceCommand command = DecreaseBalanceCommand.commandFromEvent(record.value());
        userService.internalDecrementBalance(command);

    }

}