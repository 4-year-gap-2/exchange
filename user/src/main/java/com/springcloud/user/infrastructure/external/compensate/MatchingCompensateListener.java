package com.springcloud.user.infrastructure.external.compensate;

import com.springcloud.user.application.command.UserBalanceRollBackCommand;
import com.springcloud.user.application.service.MatchingCompensationService;
import com.springcloud.user.infrastructure.dto.MatchCompensatorEvent;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class MatchingCompensateListener {


    private final MatchingCompensationService balanceCompensationService;

    public MatchingCompensateListener(MatchingCompensationService service) {
        this.balanceCompensationService = service;
    }
    //체결 실패 시 보상(자산 증가)
    @KafkaListener(
            topics = {"order_completed-to-user.execute-order-info-save-compensation"},
            containerFactory = "matchingCompensatorEventKafkaListenerContainerFactory"
    )
    public void increaseBalance(ConsumerRecord<String, MatchCompensatorEvent> record) {

        UserBalanceRollBackCommand command = UserBalanceRollBackCommand.commandFromEvent(record.value());
        balanceCompensationService.rollBack(command);

    }
}
