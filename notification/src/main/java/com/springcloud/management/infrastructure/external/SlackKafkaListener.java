package com.springcloud.management.infrastructure.external;

import com.springcloud.management.application.dto.CreateSlackCommand;
import com.springcloud.management.application.service.SlackService;
import com.springcloud.management.domain.entity.Slack;
import com.springcloud.management.infrastructure.external.dto.NotificationKafkaEvent;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import javax.management.Notification;

@Component
@RequiredArgsConstructor
public class SlackKafkaListener {

    private final SlackService slackService;
    private final SlackClientAdapter slackClientAdapter;

    @KafkaListener(
            topics = {"slack_topic"},
            containerFactory = "matchingEventKafkaListenerContainerFactory",
            concurrency = "3"  // 3개의 스레드로 병렬 처리
    )
    public void increaseBalance(ConsumerRecord<String, NotificationKafkaEvent> record) {

        CreateSlackCommand command = CreateSlackCommand.fromEvent(record.value());
        slackService.saveSlackMessage(command, command.createMessage(record.topic()));
        slackClientAdapter.sendSlackMessageToAdminChannel("초비상 당장 확인 바람" + command.createMessage(record.topic()));

    }
}
