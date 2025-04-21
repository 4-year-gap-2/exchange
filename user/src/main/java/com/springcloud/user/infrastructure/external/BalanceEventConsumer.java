package com.springcloud.user.infrastructure.external;


import com.springcloud.user.application.command.DecreaseBalanceCommand;
import com.springcloud.user.application.command.IncreaseBalanceCommand;
import com.springcloud.user.application.service.BalanceCompensationService;
import com.springcloud.user.application.service.UserService;
import com.springcloud.user.infrastructure.dto.KafkaUserBalanceDecreaseEvent;
import com.springcloud.user.infrastructure.dto.KafkaUserBalanceIncreaseEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BalanceEventConsumer {

    private final UserService userService;
    private final BalanceCompensationService balanceCompensationService;

    @KafkaListener(
            topics = {"order-to-user.excute-decrease-balance"},
            containerFactory = "orderEventKafkaListenerContainerFactory",
            concurrency = "3"  // 3개의 스레드로 병렬 처리
    )
    public void decreaseBalance(ConsumerRecord<String, KafkaUserBalanceDecreaseEvent> record) {
        log.info("Kafka로부터 메시지 수신: {}", record);
        DecreaseBalanceCommand command = DecreaseBalanceCommand.commandFromEvent(record.value());
        userService.internalDecrementBalance(command);

    }

    //체결 시 자산 증가
    @KafkaListener(
            topics = {"order_completed-to-user_balance.execute-increase-balance"},
            containerFactory = "matchingEventKafkaListenerContainerFactory",
            concurrency = "3"  // 3개의 스레드로 병렬 처리

    )
    public void IncreaseBalance(ConsumerRecord<String, KafkaUserBalanceIncreaseEvent> record) {

        IncreaseBalanceCommand command = IncreaseBalanceCommand.commandFromEvent(record.value());
        userService.internalIncrementBalance(command);

    }


}