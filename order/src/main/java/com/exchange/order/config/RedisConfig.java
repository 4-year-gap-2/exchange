package com.exchange.order.config;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.util.List;

@Configuration
public class RedisConfig {
    @Value("${spring.data.redis.host}")
    private String host;

    @Value("${spring.data.redis.port}")
    private int port;

    @Value("${spring.data.redis.username}")
    private String username;

    @Value("${spring.data.redis.password}")
    private String password;

//    @Bean
//    public RedisConnectionFactory redisConnectionFactory() {
//        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(host, port);
//        config.setUsername(username);
//        config.setPassword(password);
//        return new LettuceConnectionFactory(config);
//    }
//
//    @Bean
//    public RedisTemplate<String, List<GetHubRouteQuery>> hubRouteTemplate() {
//        RedisTemplate<String, List<GetHubRouteQuery>> template = new RedisTemplate<>();
//        template.setConnectionFactory(redisConnectionFactory());
//
//        template.setKeySerializer(RedisSerializer.string());
//        template.setHashKeySerializer(RedisSerializer.string());
//
//        ObjectMapper objectMapper = new ObjectMapper();
//        Jackson2JsonRedisSerializer<List<GetHubRouteQuery>> serializer =
//                new Jackson2JsonRedisSerializer<>(objectMapper.getTypeFactory().constructCollectionType(List.class, GetHubRouteQuery.class));
//
//        template.setHashValueSerializer(serializer);
//
//        return template;
//    }
}
