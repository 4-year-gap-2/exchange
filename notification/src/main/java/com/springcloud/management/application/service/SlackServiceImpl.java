package com.springcloud.management.application.service;


import com.springcloud.management.application.dto.*;
import com.springcloud.management.domain.entity.Slack;
import com.springcloud.management.domain.repository.SlackStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SlackServiceImpl implements SlackService {

    private final SlackStore slackStore;


    @Override
    public Slack saveSlackMessage(CreateSlackCommand requestDto,String topic) {
        UUID userId = UUID.randomUUID();
        Slack slack = Slack.createSlack(userId,topic);
        slack.create(String.valueOf(userId));
        return  slackStore.save(slack);
    }
}
