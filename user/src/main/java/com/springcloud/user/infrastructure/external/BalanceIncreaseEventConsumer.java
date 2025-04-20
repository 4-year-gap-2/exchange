package com.springcloud.user.infrastructure.external;


import com.springcloud.user.application.command.IncreaseBalanceCommand;
import com.springcloud.user.application.command.UserBalanceCommandService;
import com.springcloud.user.infrastructure.dto.KafkaUserBalanceIncreaseEvent;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BalanceIncreaseEventConsumer {

    private final UserBalanceCommandService userBalanceCommandService;


    @KafkaListener(
            topics = {"user-balance-increase"},
            groupId = "user-service",
            concurrency = "3"  // 3개의 스레드로 병렬 처리
    )
    public void increaseBalance(ConsumerRecord<String, KafkaUserBalanceIncreaseEvent> record) {

        IncreaseBalanceCommand command = IncreaseBalanceCommand.commandFromEvent(record.value());
        userBalanceCommandService.internalIncrementBalance(command);

    }

}