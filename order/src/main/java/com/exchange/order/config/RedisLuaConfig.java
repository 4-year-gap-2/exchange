package com.exchange.order.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Configuration
public class RedisLuaConfig {
    private static final Logger log = LoggerFactory.getLogger(RedisLuaConfig.class);

    @Bean
    public RedisScript<String> removeOrderScript() {
        DefaultRedisScript<String> script = new DefaultRedisScript<>();
        script.setResultType(String.class);
        try {
            ClassPathResource resource = new ClassPathResource("scripts/remove_order.lua");
            String scriptText = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            log.info("로드된 Lua 스크립트:\n{}", scriptText); // <-- 여기!
            script.setScriptText(scriptText);
            log.info("루아스크립트 로드 성공");
        } catch (Exception e) {
            throw new RuntimeException("Lua 스크립트 로드 실패", e);
        }
        return script;
    }
}
