package com.springcloud.management.infrastructure.external;

public interface SlackClientAdapter {
    void sendSlackMessageToAdminChannel(String message);
}
