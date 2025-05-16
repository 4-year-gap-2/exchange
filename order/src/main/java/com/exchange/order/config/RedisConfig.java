//package com.exchange.order.config;
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.data.redis.connection.RedisConnectionFactory;
//import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
//import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.data.redis.serializer.StringRedisSerializer;
//
//@Configuration
//public class RedisConfig {
//
//    @Value("${spring.data.redis.host}")
//    private String host;
//
//    @Value("${spring.data.redis.port}")
//    private int port;
//
//    @Value("${spring.data.redis.username}")
//    private String username;
//
//    @Value("${spring.data.redis.password}")
//    private String password;
//
//    @Bean
//    public RedisConnectionFactory redisConnectionFactory() {
//        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(host, port);
//        config.setUsername(username);
//        config.setPassword(password);
//        return new LettuceConnectionFactory(config);
//    }
//
//    @Bean
//    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory factory) {
//        RedisTemplate<String, String> template = new RedisTemplate<>();
//        template.setConnectionFactory(factory);
//
//        return template;
//    }
//
//
//
////    @Bean
////    public RedisTemplate<String, CreateMatchingCommand> redisTemplate(RedisConnectionFactory redisConnectionFactory, ObjectMapper objectMapper) {
////        RedisTemplate<String, CreateMatchingCommand> template = new RedisTemplate<>();
////        template.setConnectionFactory(redisConnectionFactory());
////
////        template.setKeySerializer(RedisSerializer.string());
////        template.setHashKeySerializer(RedisSerializer.string());
////        Jackson2JsonRedisSerializer<CreateMatchingCommand> serializer = new Jackson2JsonRedisSerializer<>(CreateMatchingCommand.class);
////        template.setValueSerializer(serializer);
////        template.setHashValueSerializer(serializer);
////
////        return template;
////    }
////
////    @Bean
////    public RedissonClient redissonClient() {
////        RedissonClient redisson;
////        Config config = new Config();
////        config.useSingleServer()
////                .setAddress("redis://" + host + ":" + port)
////                .setUsername(username)
////                .setPassword(password);
////
////        redisson = Redisson.create(config);
////        return redisson;
////    }
//}