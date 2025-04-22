//package com.springcloud.user.infrastructure.external.compensate;
//
//import com.springcloud.user.application.command.UserBalanceRollBackCommand;
//import com.springcloud.user.application.service.BalanceCompensationService;
//import com.springcloud.user.infrastructure.dto.MatchCompensatorEvent;
//import lombok.RequiredArgsConstructor;
//import org.apache.kafka.clients.consumer.ConsumerRecord;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.kafka.annotation.KafkaListener;
//import org.springframework.stereotype.Component;
//
//@Component
//@RequiredArgsConstructor
//public class CompletedOrderCompensationListener {
//
//    @Qualifier("completedOrderCompensationService")
//
//    private final BalanceCompensationService balanceCompensationService;
//
//    @KafkaListener(
//            topics = {"order_completed-to-user.execute-order-info-save-compensation"},
//            groupId = "user-service",
//            concurrency = "3"  // 3개의 스레드로 병렬 처리
//    )
//    public void increaseBalance(ConsumerRecord<String, MatchCompensatorEvent> record) {
//
//        UserBalanceRollBackCommand command = UserBalanceRollBackCommand.commandFromEvent(record.value());
//        balanceCompensationService.rollBack(command);
//
//    }
//}
