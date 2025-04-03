package com.exchange.eureka.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/**").authenticated() // Eureka 대시보드 보호
                        .anyRequest().permitAll() // 다른 요청은 허용
                )
                .httpBasic(Customizer.withDefaults()) // Basic 인증 사용
                .csrf(AbstractHttpConfigurer::disable); // CSRF 비활성화 (Eureka는 CSRF 필요 없음)
        return http.build();
    }
}