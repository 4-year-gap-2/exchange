package com.springcloud.management.application.service;


import com.springcloud.management.application.dto.CreateSlackCommand;
import com.springcloud.management.domain.entity.Slack;


public interface SlackService {
    Slack saveSlackMessage(CreateSlackCommand requestDto , String topic);
}
