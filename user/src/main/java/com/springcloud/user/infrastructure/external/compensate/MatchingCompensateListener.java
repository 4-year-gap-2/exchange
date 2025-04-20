package com.springcloud.user.infrastructure.external.compensate;

import com.springcloud.user.application.command.IncreaseBalanceCommand;
import com.springcloud.user.application.command.UserBalanceRollBackCommand;
import com.springcloud.user.application.service.BalanceCompensationService;
import com.springcloud.user.infrastructure.dto.MatchCompensatorEvent;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MatchingCompensateListener {

    private final BalanceCompensationService balanceCompensationService;

    @KafkaListener(
            topics = {"4yearGap.match.MatchCompensatorEvent.compensation"},
            groupId = "user-service",
            concurrency = "3"  // 3개의 스레드로 병렬 처리
    )
    public void increaseBalance(ConsumerRecord<String, MatchCompensatorEvent> record) {

        UserBalanceRollBackCommand command = UserBalanceRollBackCommand.commandFromEvent(record.value());
        balanceCompensationService.rollBack(command);

    }
}
