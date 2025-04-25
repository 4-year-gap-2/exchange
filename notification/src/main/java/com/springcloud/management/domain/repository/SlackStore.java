package com.springcloud.management.domain.repository;

import com.springcloud.management.domain.entity.Slack;

public interface SlackStore {
    Slack save(Slack slack);
}
