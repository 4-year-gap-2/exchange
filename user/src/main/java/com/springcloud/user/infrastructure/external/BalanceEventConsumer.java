package com.springcloud.user.infrastructure.external;


import com.springcloud.user.application.command.DecreaseBalanceCommand;
import com.springcloud.user.application.command.IncreaseBalanceCommand;
import com.springcloud.user.application.service.UserService;
import com.springcloud.user.common.exception.InsufficientBalanceException;
import com.springcloud.user.infrastructure.dto.KafkaInsufficientBalanceEvent;
import com.springcloud.user.infrastructure.dto.KafkaUserBalanceDecreaseEvent;
import com.springcloud.user.infrastructure.dto.KafkaUserBalanceIncreaseEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BalanceEventConsumer {

    private final UserService userService;
    private final KafkaTemplate<String, KafkaInsufficientBalanceEvent> kafkaTemplate;

    @KafkaListener(
            topics = {"order-to-user.execute-decrease-balance"},
            containerFactory = "balanceDecreaseKafkaListenerContainerFactory"
    )
    public void decreaseBalance(ConsumerRecord<String, KafkaUserBalanceDecreaseEvent> record) {
        log.info("Kafka로부터 메시지 수신: {}", record);
        try {
            DecreaseBalanceCommand command = DecreaseBalanceCommand.commandFromEvent(record.value());
            userService.internalDecrementBalance(command);
        } catch (Exception e) {
            if (e instanceof InsufficientBalanceException) {
                // 잔액 부족 예외일 때만 실패 큐로 전송
                log.info("잔액 부족 예외일 때만 실패 큐로 전송: {}", record);
                KafkaInsufficientBalanceEvent kafkaInsufficientBalanceEvent = KafkaInsufficientBalanceEvent.from(
                        record.value().getOrderId(), record.value().getUserId(), record.value().getPrice(), ((InsufficientBalanceException) e).getAvailableBalance());
                kafkaTemplate.send("user-to-socket.execute-balance-decrease-fail",kafkaInsufficientBalanceEvent);
            } else {
                // 그 외 예외는 별도 처리(로깅, 알림 등)
                log.error("예상치 못한 예외 발생, DLQ 이동", e);
                // 시스템 예외: throw e → DLQ로 이동
                throw e;
            }
        }
        log.info("11111");

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