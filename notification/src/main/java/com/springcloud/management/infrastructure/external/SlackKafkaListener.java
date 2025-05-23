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
            topics = {"user-to-management.execute-notification"},
            containerFactory = "matchingEventKafkaListenerContainerFactory",
            concurrency = "3"  // 3개의 스레드로 병렬 처리
    )
    public void slackListener(ConsumerRecord<String, NotificationKafkaEvent> record) {


        CreateSlackCommand command = CreateSlackCommand.fromEvent(record.value());
        String message = command.createMessage(record.topic());
        slackService.saveSlackMessage(command,message);
        slackClientAdapter.sendSlackMessageToAdminChannel(message);

    }
}
